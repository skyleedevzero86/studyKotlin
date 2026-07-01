package com.kochat.global.application.admin

import com.kochat.adapter.inbound.web.admin.dto.MessageTypeYearRow
import com.kochat.adapter.inbound.web.admin.dto.MessageTypeYearStatisticsResponse
import com.kochat.adapter.inbound.web.admin.dto.RoomTypeDailyRow
import com.kochat.adapter.inbound.web.admin.dto.RoomTypeDailyStatisticsResponse
import com.kochat.adapter.inbound.web.admin.dto.StatisticsCountRow
import com.kochat.adapter.inbound.web.admin.dto.StatisticsPeriodResponse
import com.kochat.adapter.inbound.web.admin.dto.TypeCountRatio
import com.kochat.domain.chat.model.ChatRoomType
import com.kochat.domain.chat.model.MessageType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import kotlin.math.round

@Service
class AdminStatisticsQueryService(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun getHourlyStatistics(filter: StatisticsFilter): StatisticsPeriodResponse {
        val params = mutableListOf<Any>(
            filter.fromDateTime,
            filter.toDateTimeExclusive,
        )
        val roomClause = appendRoomTypeClause(filter.roomType, params)
        val messageClause = appendMessageTypeClause(filter.messageType, params)

        val sql = """
            SELECT HOUR(m.created_at) AS slot, COUNT(*) AS cnt
            FROM messages m
            INNER JOIN chat_rooms cr ON cr.id = m.chat_room_id
            WHERE m.is_deleted = false
              AND m.created_at >= ? AND m.created_at < ?
              $roomClause
              $messageClause
            GROUP BY slot
            ORDER BY slot
        """.trimIndent()

        val countsByHour = jdbcTemplate.query(sql, { rs, _ ->
            rs.getInt("slot") to rs.getLong("cnt")
        }, *params.toTypedArray()).toMap()

        val total = countsByHour.values.sum()
        val rows = (0..23).map { hour ->
            val count = countsByHour[hour] ?: 0L
            StatisticsCountRow(
                label = formatHourSlot(hour),
                count = count,
                ratio = ratio(count, total),
            )
        }

        return StatisticsPeriodResponse(
            title = "시간대별 메시지 현황",
            from = filter.from,
            to = filter.to,
            roomType = filter.roomType?.name,
            messageType = filter.messageType?.name,
            rows = rows,
            total = total,
        )
    }

    fun getMessageTypeYearStatistics(filter: StatisticsFilter): MessageTypeYearStatisticsResponse {
        val params = mutableListOf<Any>(
            filter.fromDateTime,
            filter.toDateTimeExclusive,
        )
        val roomClause = appendRoomTypeClause(filter.roomType, params)

        val sql = """
            SELECT YEAR(m.created_at) AS yr, m.type AS msg_type, COUNT(*) AS cnt
            FROM messages m
            INNER JOIN chat_rooms cr ON cr.id = m.chat_room_id
            WHERE m.is_deleted = false
              AND m.created_at >= ? AND m.created_at < ?
              $roomClause
            GROUP BY yr, msg_type
            ORDER BY yr, msg_type
        """.trimIndent()

        val raw = jdbcTemplate.query(sql, { rs, _ ->
            Triple(rs.getInt("yr"), rs.getString("msg_type"), rs.getLong("cnt"))
        }, *params.toTypedArray())

        val typeLabels = MessageType.entries.map { it.name }
        val years = (filter.from.year..filter.to.year).toList()
        val grouped = raw.groupBy({ it.first }, { it.second to it.third })

        val rows = years.map { year ->
            val typeCounts = typeLabels.associateWith { label ->
                grouped[year]?.firstOrNull { it.first == label }?.second ?: 0L
            }
            val yearTotal = typeCounts.values.sum()
            MessageTypeYearRow(
                year = year,
                types = typeCounts.mapValues { (_, count) ->
                    TypeCountRatio(count, ratio(count, yearTotal))
                },
                total = yearTotal,
            )
        }

        val grandTotal = rows.sumOf { it.total }
        val totals = typeLabels.associateWith { label ->
            val count = rows.sumOf { it.types[label]?.count ?: 0L }
            TypeCountRatio(count, ratio(count, grandTotal))
        }

        return MessageTypeYearStatisticsResponse(
            title = "메시지 유형별 년도별 현황",
            from = filter.from,
            to = filter.to,
            roomType = filter.roomType?.name,
            typeLabels = typeLabels,
            rows = rows,
            totals = totals,
            grandTotal = grandTotal,
        )
    }

    fun getRoomTypeDailyStatistics(filter: StatisticsFilter): RoomTypeDailyStatisticsResponse {
        val params = mutableListOf<Any>(
            filter.fromDateTime,
            filter.toDateTimeExclusive,
        )
        val messageClause = appendMessageTypeClause(filter.messageType, params)

        val sql = """
            SELECT DATE(m.created_at) AS day, cr.type AS room_type, COUNT(*) AS cnt
            FROM messages m
            INNER JOIN chat_rooms cr ON cr.id = m.chat_room_id
            WHERE m.is_deleted = false
              AND m.created_at >= ? AND m.created_at < ?
              $messageClause
            GROUP BY day, room_type
            ORDER BY day, room_type
        """.trimIndent()

        val raw = jdbcTemplate.query(sql, { rs, _ ->
            Triple(rs.getDate("day").toLocalDate(), rs.getString("room_type"), rs.getLong("cnt"))
        }, *params.toTypedArray())

        val typeLabels = ChatRoomType.entries.map { it.name }
        val dates = generateSequence(filter.from) { current ->
            val next = current.plusDays(1)
            if (next <= filter.to) next else null
        }.toList()

        val grouped = raw.groupBy({ it.first }, { it.second to it.third })
        val rows = dates.map { date ->
            val typeCounts = typeLabels.associateWith { label ->
                grouped[date]?.firstOrNull { it.first == label }?.second ?: 0L
            }
            val dayTotal = typeCounts.values.sum()
            RoomTypeDailyRow(
                date = date,
                types = typeCounts.mapValues { (_, count) ->
                    TypeCountRatio(count, ratio(count, dayTotal))
                },
                total = dayTotal,
            )
        }

        val grandTotal = rows.sumOf { it.total }
        val totals = typeLabels.associateWith { label ->
            val count = rows.sumOf { it.types[label]?.count ?: 0L }
            TypeCountRatio(count, ratio(count, grandTotal))
        }

        return RoomTypeDailyStatisticsResponse(
            title = "채팅방 유형별 일자별 메시지 현황",
            from = filter.from,
            to = filter.to,
            messageType = filter.messageType?.name,
            typeLabels = typeLabels,
            rows = rows,
            totals = totals,
            grandTotal = grandTotal,
        )
    }

    private fun appendRoomTypeClause(roomType: ChatRoomType?, params: MutableList<Any>): String {
        if (roomType == null) return ""
        params.add(roomType.name)
        return "AND cr.type = ?"
    }

    private fun appendMessageTypeClause(messageType: MessageType?, params: MutableList<Any>): String {
        if (messageType == null) return ""
        params.add(messageType.name)
        return "AND m.type = ?"
    }

    private fun formatHourSlot(hour: Int): String =
        String.format("%02d:00 ~ %02d:00", hour, (hour + 1) % 24)

    private fun ratio(count: Long, total: Long): Double {
        if (total <= 0L) return 0.0
        return round(count * 10000.0 / total) / 100.0
    }
}

package com.kochat.global.application.admin

import com.kochat.adapter.inbound.web.admin.dto.TypeCountRatio
import com.kochat.adapter.inbound.web.admin.dto.UserEventDailyRow
import com.kochat.adapter.inbound.web.admin.dto.UserEventDailyStatisticsResponse
import com.kochat.domain.user.model.UserActivityEventType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import kotlin.math.round

@Service
class AdminUserStatisticsQueryService(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun getUserEventDailyStatistics(filter: UserStatisticsFilter): UserEventDailyStatisticsResponse {
        val params = mutableListOf<Any>(
            filter.fromDateTime,
            filter.toDateTimeExclusive,
        )
        val eventClause = appendEventTypeClause(filter.eventType, params)

        val sql = """
            SELECT DATE(l.occurred_at) AS day, l.event_type AS event_type, COUNT(*) AS cnt
            FROM user_activity_logs l
            WHERE l.occurred_at >= ? AND l.occurred_at < ?
              $eventClause
            GROUP BY day, event_type
            ORDER BY day, event_type
        """.trimIndent()

        val raw = jdbcTemplate.query(sql, { rs, _ ->
            Triple(rs.getDate("day").toLocalDate(), rs.getString("event_type"), rs.getLong("cnt"))
        }, *params.toTypedArray())

        val typeLabels = UserActivityEventType.entries.map { it.name }
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
            UserEventDailyRow(
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

        return UserEventDailyStatisticsResponse(
            title = "사용자 활동 일자별 현황",
            from = filter.from,
            to = filter.to,
            eventType = filter.eventType?.name,
            typeLabels = typeLabels,
            rows = rows,
            totals = totals,
            grandTotal = grandTotal,
        )
    }

    private fun appendEventTypeClause(eventType: UserActivityEventType?, params: MutableList<Any>): String {
        if (eventType == null) return ""
        params.add(eventType.name)
        return "AND l.event_type = ?"
    }

    private fun ratio(count: Long, total: Long): Double {
        if (total <= 0L) return 0.0
        return round(count * 10000.0 / total) / 100.0
    }
}

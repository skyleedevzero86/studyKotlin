package com.sleekydz86.cdcmigration.migration.infrastructure.kafka

import com.sleekydz86.cdcmigration.migration.domain.cdc.ChangeEvent
import com.sleekydz86.cdcmigration.migration.domain.cdc.ChangeOperation
import com.sleekydz86.cdcmigration.migration.domain.cdc.EventPosition
import com.sleekydz86.cdcmigration.migration.domain.cdc.LegacyTable
import com.sleekydz86.cdcmigration.migration.domain.cdc.SnapshotNotification
import org.springframework.stereotype.Component
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

sealed interface ParsedMessage {
    data class Change(val event: ChangeEvent, val payload: String) : ParsedMessage
    data class Notification(val notification: SnapshotNotification) : ParsedMessage
    data class Ignored(val reason: String) : ParsedMessage
}

@Component
class DebeziumMessageParser {

    private val json = JsonMapper.builder().build()
    private val timestampFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun parse(topic: String, partition: Int, offset: Long, value: String?): ParsedMessage {
        if (value.isNullOrBlank()) {
            return ParsedMessage.Ignored("값이 없는 메시지(톰스톤)")
        }
        val prefix = topic.substringBefore('.')
        val root = unwrap(json.readTree(value))
        if (topic.endsWith(".notifications")) {
            val data = root.path("additional_data").takeUnless { it.isMissingNode } ?: root.path("additionalData")
            val type = root.text("type") ?: return ParsedMessage.Ignored("알림 종류가 없는 메시지")
            return ParsedMessage.Notification(
                SnapshotNotification(prefix, type, data.text("data_collection") ?: data.text("scanned_collection"), data.text("total_rows_scanned")?.toLongOrNull()),
            )
        }
        val op = root.text("op")?.let { ChangeOperation.of(it) } ?: return ParsedMessage.Ignored("op가 없는 메시지")
        val tableName = root.path("source").text("table") ?: topic.substringAfterLast('.')
        val table = LegacyTable.ofTableName(tableName) ?: return ParsedMessage.Ignored("이관 대상이 아닌 테이블 $tableName")
        val before = rowOf(root.path("before"), table)
        val after = rowOf(root.path("after"), table)
        val lsn = root.path("source").path("lsn").takeIf { it.isNumber }?.asLong()
        val event = ChangeEvent(prefix, EventPosition(topic, partition, offset), table, op, before, after, lsn)
        return ParsedMessage.Change(event, json.writeValueAsString(mapOf("before" to before, "after" to after)))
    }

    private fun unwrap(node: JsonNode): JsonNode = if (node.has("schema") && node.has("payload")) node.path("payload") else node

    private fun rowOf(node: JsonNode, table: LegacyTable): Map<String, String?>? {
        if (node.isMissingNode || node.isNull) {
            return null
        }
        return node.properties().associate { (column, value) -> column to convert(column, value, table) }
    }

    private fun convert(column: String, value: JsonNode, table: LegacyTable): String? = when {
        value.isNull -> null
        column in table.timestampColumns && value.isNumber ->
            LocalDateTime.ofInstant(Instant.ofEpochMilli(value.asLong()), ZoneOffset.UTC).format(timestampFormat)
        column in table.dateColumns && value.isNumber -> LocalDate.ofEpochDay(value.asLong()).toString()
        value.isNumber || value.isBoolean -> value.asString()
        value.isObject || value.isArray -> value.toString()
        else -> value.asString()
    }

    private fun JsonNode.text(field: String): String? {
        val node = path(field)
        if (node.isMissingNode || node.isNull) {
            return null
        }
        return node.asString().takeIf { it.isNotEmpty() }
    }
}

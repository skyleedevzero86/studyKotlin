package com.sleekydz86.cdcmigration.migration.domain.cdc

import com.sleekydz86.cdcmigration.migration.domain.StagingTable

enum class LegacyTable(
    val tableName: String,
    val label: String,
    val keyColumns: List<String>,
    val timestampColumns: Set<String>,
    val dateColumns: Set<String>,
    val stagingTable: StagingTable?,
) {
    USER("tb_user", "사용자", listOf("inst_code", "user_id"), emptySet(), emptySet(), StagingTable.USER),
    PROJECT("tb_project", "과제", listOf("inst_code", "research_no"), emptySet(), setOf("start_date", "end_date"), StagingTable.PROJECT),
    SUBMISSION("tb_appl", "심의 신청", listOf("appl_seq"), setOf("submitted_at", "updated_at"), emptySet(), StagingTable.SUBMISSION),
    REVIEW("tb_review", "심의 이력", listOf("inst_code", "review_id"), setOf("reviewed_at"), emptySet(), StagingTable.REVIEW),
    ATTACHMENT("tb_attach", "첨부파일", listOf("inst_code", "file_id"), emptySet(), emptySet(), null);

    companion object {
        fun ofTableName(name: String): LegacyTable? = entries.firstOrNull { it.tableName == name }
    }
}

enum class ChangeOperation(val code: String, val label: String) {
    READ("r", "스냅샷"),
    CREATE("c", "등록"),
    UPDATE("u", "수정"),
    DELETE("d", "삭제");

    companion object {
        fun of(code: String): ChangeOperation? = entries.firstOrNull { it.code == code }
    }
}

data class EventPosition(val topic: String, val partition: Int, val offset: Long)

data class ChangeEvent(
    val topicPrefix: String,
    val position: EventPosition,
    val table: LegacyTable,
    val op: ChangeOperation,
    val before: Map<String, String?>?,
    val after: Map<String, String?>?,
    val sourceLsn: Long?,
) {
    val snapshot: Boolean get() = op == ChangeOperation.READ

    val row: Map<String, String?> get() = after ?: before ?: emptyMap()

    fun sourceKey(): String = table.keyColumns.joinToString("/") { row[it] ?: "" }

    fun eventKey(): String =
        if (snapshot) "r:${table.tableName}:${sourceKey()}"
        else "${op.code}:${table.tableName}:${sourceKey()}:${sourceLsn ?: "offset-${position.offset}"}"
}

data class SnapshotNotification(
    val topicPrefix: String,
    val type: String,
    val dataCollection: String?,
    val totalRows: Long?,
) {
    fun table(): LegacyTable? = dataCollection?.substringAfterLast('.')?.let { LegacyTable.ofTableName(it) }
}

data class StoredCdcEvent(
    val eventId: Long,
    val cdcRunId: Long,
    val table: LegacyTable,
    val op: ChangeOperation,
    val before: Map<String, String?>?,
    val after: Map<String, String?>?,
    val attempts: Int,
) {
    val row: Map<String, String?> get() = after ?: before ?: emptyMap()
}

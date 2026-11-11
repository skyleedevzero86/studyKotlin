package com.sleekydz86.cdcmigration.migration.domain.cdc

data class SnapshotTableProgress(
    val table: LegacyTable,
    val expectedRows: Long?,
    val scanCompleted: Boolean,
    val stagedRows: Long,
)

class SnapshotGate(private val completedNotified: Boolean, private val progress: List<SnapshotTableProgress>) {

    fun blockers(): List<String> {
        val reasons = mutableListOf<String>()
        if (!completedNotified) {
            reasons += "Debezium 초기 스냅샷 완료 알림(COMPLETED)을 기다리는 중"
        }
        LegacyTable.entries.forEach { table ->
            val current = progress.firstOrNull { it.table == table }
            when {
                current == null || !current.scanCompleted ->
                    reasons += "${table.label}(${table.tableName}) 테이블 스캔 완료 알림을 기다리는 중"
                current.expectedRows == null ->
                    reasons += "${table.label}(${table.tableName}) 스캔 건수를 알 수 없음"
                current.stagedRows < current.expectedRows ->
                    reasons += "${table.label}(${table.tableName}) 스냅샷 이벤트 ${current.stagedRows}/${current.expectedRows}건만 반영됨"
            }
        }
        return reasons
    }

    fun ready(): Boolean = blockers().isEmpty()
}

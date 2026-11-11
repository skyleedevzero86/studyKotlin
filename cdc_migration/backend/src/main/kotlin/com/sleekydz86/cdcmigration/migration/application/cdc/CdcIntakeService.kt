package com.sleekydz86.cdcmigration.migration.application.cdc

import com.sleekydz86.cdcmigration.migration.application.CdcEventRepository
import com.sleekydz86.cdcmigration.migration.application.CdcRunRepository
import com.sleekydz86.cdcmigration.migration.application.FileTransferRequester
import com.sleekydz86.cdcmigration.migration.application.StagingRepository
import com.sleekydz86.cdcmigration.migration.domain.LegacyRowMapper
import com.sleekydz86.cdcmigration.migration.domain.cdc.CdcEventStatus
import com.sleekydz86.cdcmigration.migration.domain.cdc.CdcRunStatus
import com.sleekydz86.cdcmigration.migration.domain.cdc.ChangeEvent
import com.sleekydz86.cdcmigration.migration.domain.cdc.LegacyTable
import com.sleekydz86.cdcmigration.migration.domain.cdc.SnapshotNotification
import com.sleekydz86.cdcmigration.migration.domain.cdc.StoredCdcEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class CdcIntakeService(
    private val cdcRuns: CdcRunRepository,
    private val events: CdcEventRepository,
    private val staging: StagingRepository,
    private val fileTransfers: FileTransferRequester,
    private val applier: CdcChangeApplier,
    private val clock: Clock,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun receive(event: ChangeEvent, payload: String): String {
        val run = cdcRuns.findByTopicPrefix(event.topicPrefix)
            ?: return "알 수 없는 CDC 실행(${event.topicPrefix})의 이벤트라 건너뜀"
        if (!run.status.active) {
            return "${run.status.label} 상태 실행의 이벤트라 건너뜀"
        }
        if (event.snapshot) {
            val eventId = events.insertIfAbsent(run.id, event, payload, CdcEventStatus.STAGED)
                ?: return "중복 수신한 스냅샷 이벤트 ${event.eventKey()}"
            if (event.table == LegacyTable.ATTACHMENT) {
                fileTransfers.requestFromCdc(run.id, event.row)
            } else {
                val stagingTable = event.table.stagingTable!!
                val rowNo = staging.nextRowNo(run.migrationRunId, stagingTable)
                staging.insert(run.migrationRunId, listOf(LegacyRowMapper.toStagedRow(stagingTable, rowNo, event.row)))
            }
            return "스냅샷 이벤트 $eventId 스테이징 적재"
        }
        val eventId = events.insertIfAbsent(run.id, event, payload, CdcEventStatus.PENDING)
            ?: return "중복 수신한 변경 이벤트 ${event.eventKey()}"
        if (run.status == CdcRunStatus.STREAMING && !events.hasPendingBefore(run.id, eventId)) {
            val outcome = applier.apply(run, StoredCdcEvent(eventId, run.id, event.table, event.op, event.before, event.after, 0))
            events.updateStatus(eventId, outcome.status, outcome.message)
            return "변경 이벤트 $eventId ${outcome.status.label}: ${outcome.message}"
        }
        return "변경 이벤트 $eventId 보관 (초기 적재 후 순서대로 반영)"
    }

    @Transactional
    fun receive(notification: SnapshotNotification): String {
        val run = cdcRuns.findByTopicPrefix(notification.topicPrefix)
            ?: return "알 수 없는 CDC 실행(${notification.topicPrefix})의 알림이라 건너뜀"
        when (notification.type) {
            "TABLE_SCAN_COMPLETED" -> notification.table()?.let { cdcRuns.recordTableScan(run.id, it, notification.totalRows) }
            "COMPLETED" -> {
                run.markSnapshotCompleted(clock.instant())
                cdcRuns.save(run)
            }
            "ABORTED" -> {
                run.fail("Debezium 초기 스냅샷이 중단되었습니다.")
                cdcRuns.save(run)
            }
        }
        log.info("CDC 실행 {} 스냅샷 알림: {} {} {}", run.id, notification.type, notification.dataCollection ?: "", notification.totalRows ?: "")
        return "알림 ${notification.type} 기록"
    }
}

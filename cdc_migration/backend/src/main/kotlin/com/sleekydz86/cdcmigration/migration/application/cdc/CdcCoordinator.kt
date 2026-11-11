package com.sleekydz86.cdcmigration.migration.application.cdc

import com.sleekydz86.cdcmigration.global.config.AppProperties
import com.sleekydz86.cdcmigration.global.procedure.Worker
import com.sleekydz86.cdcmigration.global.procedure.WorkerContext
import com.sleekydz86.cdcmigration.migration.application.CdcEventRepository
import com.sleekydz86.cdcmigration.migration.application.CdcRunRepository
import com.sleekydz86.cdcmigration.migration.application.MigrationExecutionService
import com.sleekydz86.cdcmigration.migration.domain.cdc.CdcEventStatus
import com.sleekydz86.cdcmigration.migration.domain.cdc.CdcRun
import com.sleekydz86.cdcmigration.migration.domain.cdc.CdcRunStatus
import com.sleekydz86.cdcmigration.migration.domain.cdc.SnapshotGate
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock

@Component
class CdcCoordinator(
    private val cdcRuns: CdcRunRepository,
    private val events: CdcEventRepository,
    private val execution: MigrationExecutionService,
    private val applier: CdcChangeApplier,
    private val properties: AppProperties,
    private val clock: Clock,
    transactionManager: PlatformTransactionManager,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val transaction = TransactionTemplate(transactionManager)

    @Scheduled(fixedDelayString = "\${app.cdc.poll-interval-ms:2000}")
    fun tick() {
        cdcRuns.findActive().forEach { run ->
            runCatching { WorkerContext.runAs(Worker.CDC) { process(run.id) } }
                .onFailure { log.warn("CDC 실행 {} 처리 중 오류: {}", run.id, it.message) }
        }
    }

    @Synchronized
    fun process(cdcRunId: Long) {
        val run = cdcRuns.find(cdcRunId) ?: return
        if (run.status == CdcRunStatus.SNAPSHOTTING) {
            tryInitialLoad(run)
        }
        if (run.status == CdcRunStatus.STREAMING) {
            drainPending(run)
            retryWaiting(run)
        }
    }

    fun gate(run: CdcRun): SnapshotGate = SnapshotGate(run.snapshotCompleted, cdcRuns.snapshotProgress(run.id))

    private fun tryInitialLoad(run: CdcRun) {
        val gate = gate(run)
        if (!gate.ready()) {
            return
        }
        transaction.execute {
            run.beginInitialLoad()
            cdcRuns.save(run)
        }
        log.info("CDC 실행 {}: 모든 스냅샷 이벤트가 스테이징에 반영되어 초기 적재를 시작합니다.", run.id)
        val result = execution.execute(run.migrationRunId)
        transaction.execute {
            if (result.status.succeeded) {
                run.startStreaming(clock.instant())
            } else {
                run.fail("초기 적재 실패: ${result.errorMessage}")
            }
            cdcRuns.save(run)
        }
        log.info("CDC 실행 {}: 초기 적재 결과 {} → {}", run.id, result.status.label, run.status.label)
    }

    private fun drainPending(run: CdcRun) {
        while (true) {
            val batch = events.findByStatus(run.id, CdcEventStatus.PENDING, 200)
            if (batch.isEmpty()) {
                return
            }
            for (event in batch) {
                try {
                    transaction.execute {
                        val outcome = applier.apply(run, event)
                        events.updateStatus(event.eventId, outcome.status, outcome.message)
                    }
                } catch (exception: Exception) {
                    log.warn("CDC 이벤트 {} 반영 실패, 다음 주기에 순서대로 다시 시도합니다: {}", event.eventId, exception.message)
                    return
                }
            }
        }
    }

    private fun retryWaiting(run: CdcRun) {
        events.findByStatus(run.id, CdcEventStatus.WAITING, 200)
            .filter { it.attempts < properties.cdc.maxWaitingAttempts }
            .forEach { event ->
                runCatching {
                    transaction.execute {
                        val outcome = applier.apply(run, event)
                        events.updateStatus(event.eventId, outcome.status, outcome.message)
                    }
                }.onFailure { log.warn("대기 이벤트 {} 재처리 실패: {}", event.eventId, it.message) }
            }
    }
}

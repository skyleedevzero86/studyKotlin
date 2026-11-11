package com.sleekydz86.cdcmigration.migration.application

import com.sleekydz86.cdcmigration.global.demo.DemoFaults
import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import com.sleekydz86.cdcmigration.migration.domain.MigrationRun
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import java.time.Clock

@Service
class MigrationExecutionService(
    private val runs: MigrationRunRepository,
    private val procedures: MigrationProcedures,
    private val faults: DemoFaults,
    private val clock: Clock,
    transactionManager: PlatformTransactionManager,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val transaction = TransactionTemplate(transactionManager).apply {
        propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
    }

    fun execute(runId: Long): MigrationRun {
        val run = transaction.execute {
            val loaded = runs.find(runId) ?: throw BusinessException(ErrorCode.MIGRATION_RUN_NOT_FOUND, runId.toString())
            loaded.start(clock.instant())
            runs.save(loaded)
            loaded
        }!!
        log.info("이관 작업 {} 실행 시작 ({}회차)", runId, run.attemptCount)
        return try {
            val validation = transaction.execute { procedures.validate(runId) }!!
            val counts = transaction.execute {
                val inserted = procedures.apply(runId)
                if (faults.consumeMigrationFailure()) {
                    throw IllegalStateException("실습용 장애 주입: 프로시저 실행 직후 실패시켜 이번 적재를 모두 롤백했습니다.")
                }
                inserted
            }!!
            transaction.execute {
                run.complete(validation, counts, clock.instant())
                runs.save(run)
            }
            log.info("이관 작업 {} 완료. 검증 오류 {}건 제외, 새로 넣은 건수={}", runId, validation.errorCount, counts)
            run
        } catch (exception: Exception) {
            val message = rootMessage(exception)
            transaction.execute {
                run.fail(message, clock.instant())
                runs.save(run)
            }
            log.warn("이관 작업 {} 실패: {}", runId, message)
            run
        }
    }

    private fun rootMessage(exception: Throwable): String {
        var current: Throwable = exception
        while (current.cause != null && current.cause !== current) {
            current = current.cause!!
        }
        return current.message ?: current.javaClass.simpleName
    }
}

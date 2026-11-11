package com.sleekydz86.cdcmigration.migration.domain

import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import java.time.Instant

enum class MigrationMode(val label: String) {
    CSV("CSV 일회성 이관"),
    CDC("CDC 초기 스냅샷"),
}

enum class MigrationRunStatus(val label: String) {
    CREATED("적재 대기"),
    RUNNING("실행 중"),
    COMPLETED("완료"),
    COMPLETED_WITH_ISSUES("완료 (검증 오류 행 제외)"),
    FAILED("실패");

    val succeeded: Boolean get() = this == COMPLETED || this == COMPLETED_WITH_ISSUES
}

data class ValidationOutcome(val errorCount: Int, val infoCount: Int)

data class MigrationCounts(val users: Int = 0, val projects: Int = 0, val submissions: Int = 0, val reviews: Int = 0)

class MigrationRun(
    val id: Long,
    val mode: MigrationMode,
    status: MigrationRunStatus,
    val description: String?,
    val createdAt: Instant,
    startedAt: Instant?,
    finishedAt: Instant?,
    attemptCount: Int,
    errorMessage: String?,
    validation: ValidationOutcome,
    counts: MigrationCounts,
) {
    var status: MigrationRunStatus = status
        private set
    var startedAt: Instant? = startedAt
        private set
    var finishedAt: Instant? = finishedAt
        private set
    var attemptCount: Int = attemptCount
        private set
    var errorMessage: String? = errorMessage
        private set
    var validation: ValidationOutcome = validation
        private set
    var counts: MigrationCounts = counts
        private set

    fun start(now: Instant) {
        if (status == MigrationRunStatus.RUNNING) {
            throw BusinessException(ErrorCode.MIGRATION_ALREADY_RUNNING, "작업 $id")
        }
        status = MigrationRunStatus.RUNNING
        startedAt = now
        finishedAt = null
        attemptCount += 1
        errorMessage = null
    }

    fun complete(validation: ValidationOutcome, counts: MigrationCounts, now: Instant) {
        check(status == MigrationRunStatus.RUNNING) { "실행 중인 작업만 완료할 수 있습니다." }
        this.validation = validation
        this.counts = counts
        status = if (validation.errorCount > 0) MigrationRunStatus.COMPLETED_WITH_ISSUES else MigrationRunStatus.COMPLETED
        finishedAt = now
    }

    fun fail(message: String, now: Instant) {
        status = MigrationRunStatus.FAILED
        errorMessage = message
        finishedAt = now
    }
}

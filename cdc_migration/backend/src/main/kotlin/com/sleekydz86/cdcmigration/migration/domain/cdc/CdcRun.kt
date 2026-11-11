package com.sleekydz86.cdcmigration.migration.domain.cdc

import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import java.time.Instant

enum class CdcRunStatus(val label: String) {
    SNAPSHOTTING("초기 스냅샷 수신 중"),
    LOADING("초기 적재 중"),
    STREAMING("변경분 반영 중"),
    STOPPED("중지"),
    FAILED("실패");

    val active: Boolean get() = this == SNAPSHOTTING || this == LOADING || this == STREAMING
}

enum class CdcEventStatus(val label: String) {
    STAGED("스테이징 적재"),
    PENDING("초기 적재 대기"),
    APPLIED("반영"),
    WAITING("재처리 대기"),
    SKIPPED("건너뜀"),
    FAILED("실패"),
}

data class ApplyOutcome(val status: CdcEventStatus, val message: String) {
    companion object {
        fun applied(message: String) = ApplyOutcome(CdcEventStatus.APPLIED, message)
        fun waiting(message: String) = ApplyOutcome(CdcEventStatus.WAITING, message)
        fun skipped(message: String) = ApplyOutcome(CdcEventStatus.SKIPPED, message)
        fun failed(message: String) = ApplyOutcome(CdcEventStatus.FAILED, message)
    }
}

class CdcRun(
    val id: Long,
    val migrationRunId: Long,
    val topicPrefix: String,
    val connectorName: String,
    status: CdcRunStatus,
    snapshotCompleted: Boolean,
    snapshotCompletedAt: Instant?,
    initialLoadAt: Instant?,
    lastError: String?,
    val createdAt: Instant,
) {
    var status: CdcRunStatus = status
        private set
    var snapshotCompleted: Boolean = snapshotCompleted
        private set
    var snapshotCompletedAt: Instant? = snapshotCompletedAt
        private set
    var initialLoadAt: Instant? = initialLoadAt
        private set
    var lastError: String? = lastError
        private set

    fun markSnapshotCompleted(now: Instant) {
        if (!snapshotCompleted) {
            snapshotCompleted = true
            snapshotCompletedAt = now
        }
    }

    fun beginInitialLoad() {
        if (status != CdcRunStatus.SNAPSHOTTING) {
            throw BusinessException(ErrorCode.CDC_STATE, "초기 적재는 스냅샷 수신 중에만 시작합니다. 현재: ${status.label}")
        }
        status = CdcRunStatus.LOADING
    }

    fun startStreaming(now: Instant) {
        status = CdcRunStatus.STREAMING
        initialLoadAt = now
        lastError = null
    }

    fun retryInitialLoad() {
        if (status != CdcRunStatus.FAILED || initialLoadAt != null) {
            throw BusinessException(ErrorCode.CDC_STATE, "초기 적재에 실패한 실행만 다시 시도할 수 있습니다.")
        }
        status = CdcRunStatus.SNAPSHOTTING
        lastError = null
    }

    fun fail(message: String) {
        status = CdcRunStatus.FAILED
        lastError = message
    }

    fun stop() {
        status = CdcRunStatus.STOPPED
    }
}

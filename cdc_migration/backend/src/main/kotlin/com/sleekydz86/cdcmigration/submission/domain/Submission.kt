package com.sleekydz86.cdcmigration.submission.domain

import java.time.Instant

data class LegacySubmissionKey(val instCode: String, val regnoold: String) {
    override fun toString(): String = "$instCode/$regnoold"
}

data class LegacySubmissionChange(
    val title: String,
    val status: SubmissionStatus,
    val projectNo: String,
    val updatedAt: Instant?,
)

sealed interface ChangeResult {
    data object Applied : ChangeResult
    data class Ignored(val reason: String) : ChangeResult
    data class Rejected(val reason: String) : ChangeResult
}

class Submission(
    val subNo: Long,
    val key: LegacySubmissionKey,
    projectNo: String,
    val applicantUserId: String,
    title: String,
    status: SubmissionStatus,
    val submittedAt: Instant?,
    updatedAt: Instant?,
    deleted: Boolean,
    val version: Int,
) {
    var projectNo: String = projectNo
        private set
    var title: String = title
        private set
    var status: SubmissionStatus = status
        private set
    var updatedAt: Instant? = updatedAt
        private set
    var deleted: Boolean = deleted
        private set

    fun applyLegacyChange(change: LegacySubmissionChange): ChangeResult {
        if (deleted) {
            return ChangeResult.Rejected("삭제 처리된 신청 $key 에는 변경을 반영하지 않습니다.")
        }
        val current = updatedAt
        if (current != null && change.updatedAt != null && change.updatedAt.isBefore(current)) {
            return ChangeResult.Ignored("이미 반영된 변경보다 오래된 이벤트입니다 (${change.updatedAt} < $current).")
        }
        if (!status.canMoveTo(change.status)) {
            return ChangeResult.Rejected("상태를 ${status.label}에서 ${change.status.label}(으)로 바꿀 수 없습니다.")
        }
        title = change.title
        status = change.status
        projectNo = change.projectNo
        updatedAt = change.updatedAt ?: updatedAt
        return ChangeResult.Applied
    }

    fun markDeleted(at: Instant?): ChangeResult {
        if (deleted) {
            return ChangeResult.Ignored("이미 삭제 처리된 신청입니다.")
        }
        deleted = true
        updatedAt = at ?: updatedAt
        return ChangeResult.Applied
    }
}

data class NewSubmission(
    val key: LegacySubmissionKey,
    val projectNo: String,
    val applicantUserId: String,
    val title: String,
    val status: SubmissionStatus,
    val submittedAt: Instant?,
    val updatedAt: Instant?,
    val source: String,
    val migrationRunId: Long?,
)

package com.sleekydz86.cdcmigration.submission.domain

enum class SubmissionStatus(val label: String) {
    RECEIVED("접수"),
    UNDER_REVIEW("심의중"),
    APPROVED("승인"),
    REJECTED("반려"),
    WITHDRAWN("취소");

    fun canMoveTo(next: SubmissionStatus): Boolean = next == this || next in allowedNext()

    private fun allowedNext(): Set<SubmissionStatus> = when (this) {
        RECEIVED -> setOf(UNDER_REVIEW, APPROVED, REJECTED, WITHDRAWN)
        UNDER_REVIEW -> setOf(RECEIVED, APPROVED, REJECTED, WITHDRAWN)
        APPROVED -> setOf(WITHDRAWN)
        REJECTED -> setOf(RECEIVED, UNDER_REVIEW)
        WITHDRAWN -> emptySet()
    }
}

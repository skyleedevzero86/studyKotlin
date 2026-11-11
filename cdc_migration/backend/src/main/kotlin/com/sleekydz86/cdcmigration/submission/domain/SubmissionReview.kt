package com.sleekydz86.cdcmigration.submission.domain

import java.time.Instant

enum class ReviewResult(val label: String) {
    APPROVE("승인"),
    APPROVE_WITH_CHANGES("수정 후 승인"),
    REJECT("반려"),
}

data class ReviewUpsert(
    val subNo: Long,
    val instCode: String,
    val legacyReviewId: String,
    val reviewerUserId: String?,
    val result: ReviewResult,
    val opinion: String?,
    val reviewedAt: Instant?,
    val source: String,
    val migrationRunId: Long?,
)

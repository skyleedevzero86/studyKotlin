package com.sleekydz86.cdcmigration.submission.application

import com.sleekydz86.cdcmigration.submission.domain.LegacySubmissionKey
import com.sleekydz86.cdcmigration.submission.domain.NewSubmission
import com.sleekydz86.cdcmigration.submission.domain.ReviewUpsert
import com.sleekydz86.cdcmigration.submission.domain.Submission
import java.time.Instant

interface SubmissionRepository {
    fun findByKey(key: LegacySubmissionKey): Submission?
    fun findBySubNo(subNo: Long): Submission?
    fun insert(submission: NewSubmission): Long
    fun update(submission: Submission)
    fun upsertReview(review: ReviewUpsert)
    fun deleteReview(instCode: String, legacyReviewId: String): Int
    fun search(instCode: String?, status: String?, keyword: String?): List<SubmissionSummary>
    fun findDetail(subNo: Long): SubmissionDetail?
    fun findReviews(subNo: Long): List<ReviewView>
}

data class SubmissionSummary(
    val subNo: Long,
    val instCode: String,
    val regnoold: String,
    val title: String,
    val status: String,
    val statusLabel: String,
    val projectNo: String,
    val applicantName: String?,
    val source: String,
    val deleted: Boolean,
    val updatedAt: Instant?,
    val reviewCount: Int,
    val attachmentCount: Int,
)

data class SubmissionDetail(
    val subNo: Long,
    val instCode: String,
    val regnoold: String,
    val title: String,
    val status: String,
    val statusLabel: String,
    val projectNo: String,
    val projectTitle: String?,
    val virtualProject: Boolean,
    val applicantUserId: String,
    val applicantName: String?,
    val submittedAt: Instant?,
    val updatedAt: Instant?,
    val source: String,
    val migrationRunId: Long?,
    val deleted: Boolean,
    val version: Int,
)

data class ReviewView(
    val reviewNo: Long,
    val legacyReviewId: String,
    val reviewerUserId: String?,
    val reviewerName: String?,
    val result: String,
    val resultLabel: String,
    val opinion: String?,
    val reviewedAt: Instant?,
    val source: String,
)

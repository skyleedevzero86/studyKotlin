package com.sleekydz86.cdcmigration.submission.infrastructure

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.time.Instant

@Mapper
interface SubmissionMapper {
    fun findByKey(@Param("instCode") instCode: String, @Param("regnoold") regnoold: String): SubmissionRow?
    fun findBySubNo(@Param("subNo") subNo: Long): SubmissionRow?
    fun reviewExists(@Param("instCode") instCode: String, @Param("legacyReviewId") legacyReviewId: String): Boolean
    fun search(@Param("instCode") instCode: String?, @Param("status") status: String?, @Param("keyword") keyword: String?): List<SubmissionSummaryRow>
    fun findDetail(@Param("subNo") subNo: Long): SubmissionDetailRow?
    fun findReviews(@Param("subNo") subNo: Long): List<ReviewRow>
}

data class SubmissionRow(
    var subNo: Long = 0,
    var instCode: String = "",
    var regnoold: String = "",
    var projectNo: String = "",
    var applicantUserId: String = "",
    var title: String = "",
    var status: String = "",
    var submittedAt: Instant? = null,
    var updatedAt: Instant? = null,
    var deleted: Boolean = false,
    var version: Int = 0,
)

data class SubmissionSummaryRow(
    var subNo: Long = 0,
    var instCode: String = "",
    var regnoold: String = "",
    var title: String = "",
    var status: String = "",
    var projectNo: String = "",
    var applicantName: String? = null,
    var source: String = "",
    var deleted: Boolean = false,
    var updatedAt: Instant? = null,
    var reviewCount: Int = 0,
    var attachmentCount: Int = 0,
)

data class SubmissionDetailRow(
    var subNo: Long = 0,
    var instCode: String = "",
    var regnoold: String = "",
    var title: String = "",
    var status: String = "",
    var projectNo: String = "",
    var projectTitle: String? = null,
    var virtualProject: Boolean = false,
    var applicantUserId: String = "",
    var applicantName: String? = null,
    var submittedAt: Instant? = null,
    var updatedAt: Instant? = null,
    var source: String = "",
    var migrationRunId: Long? = null,
    var deleted: Boolean = false,
    var version: Int = 0,
)

data class ReviewRow(
    var reviewNo: Long = 0,
    var legacyReviewId: String = "",
    var reviewerUserId: String? = null,
    var reviewerName: String? = null,
    var result: String = "",
    var opinion: String? = null,
    var reviewedAt: Instant? = null,
    var source: String = "",
)

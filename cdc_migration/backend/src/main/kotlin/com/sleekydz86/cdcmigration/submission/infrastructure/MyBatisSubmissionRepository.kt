package com.sleekydz86.cdcmigration.submission.infrastructure

import com.sleekydz86.cdcmigration.global.procedure.CudProcedureMapper
import com.sleekydz86.cdcmigration.global.procedure.ReviewCudCall
import com.sleekydz86.cdcmigration.global.procedure.SubmissionCudCall
import com.sleekydz86.cdcmigration.global.procedure.WorkerContext
import com.sleekydz86.cdcmigration.submission.application.ReviewView
import com.sleekydz86.cdcmigration.submission.application.SubmissionDetail
import com.sleekydz86.cdcmigration.submission.application.SubmissionRepository
import com.sleekydz86.cdcmigration.submission.application.SubmissionSummary
import com.sleekydz86.cdcmigration.submission.domain.LegacySubmissionKey
import com.sleekydz86.cdcmigration.submission.domain.NewSubmission
import com.sleekydz86.cdcmigration.submission.domain.ReviewResult
import com.sleekydz86.cdcmigration.submission.domain.ReviewUpsert
import com.sleekydz86.cdcmigration.submission.domain.Submission
import com.sleekydz86.cdcmigration.submission.domain.SubmissionStatus
import org.springframework.stereotype.Repository

@Repository
class MyBatisSubmissionRepository(
    private val mapper: SubmissionMapper,
    private val procedures: CudProcedureMapper,
) : SubmissionRepository {

    override fun findByKey(key: LegacySubmissionKey): Submission? = mapper.findByKey(key.instCode, key.regnoold)?.toDomain()

    override fun findBySubNo(subNo: Long): Submission? = mapper.findBySubNo(subNo)?.toDomain()

    override fun insert(submission: NewSubmission): Long {
        val worker = WorkerContext.current()
        val call = SubmissionCudCall(
            "C", worker.name, worker.ip, null, submission.key.instCode, submission.key.regnoold, submission.projectNo,
            submission.applicantUserId, submission.title, submission.status.name, submission.submittedAt, submission.updatedAt,
            submission.source, submission.migrationRunId, 0,
        )
        procedures.submissionCud(call)
        call.verify()
        return call.outSubNo!!
    }

    override fun update(submission: Submission) {
        val worker = WorkerContext.current()
        val call = SubmissionCudCall(
            if (submission.deleted) "D" else "U", worker.name, worker.ip, submission.subNo, submission.key.instCode,
            submission.key.regnoold, submission.projectNo, submission.applicantUserId, submission.title, submission.status.name,
            submission.submittedAt, submission.updatedAt, null, null, submission.version,
        )
        procedures.submissionCud(call)
        call.verify()
    }

    override fun upsertReview(review: ReviewUpsert) {
        val worker = WorkerContext.current()
        val operation = if (mapper.reviewExists(review.instCode, review.legacyReviewId)) "U" else "C"
        val call = ReviewCudCall(
            operation, worker.name, worker.ip, review.instCode, review.legacyReviewId, review.subNo, review.reviewerUserId,
            review.result.name, review.opinion, review.reviewedAt, review.source, review.migrationRunId,
        )
        procedures.reviewCud(call)
        call.verify()
    }

    override fun deleteReview(instCode: String, legacyReviewId: String): Int {
        if (!mapper.reviewExists(instCode, legacyReviewId)) {
            return 0
        }
        val worker = WorkerContext.current()
        val call = ReviewCudCall("D", worker.name, worker.ip, instCode, legacyReviewId, null, null, null, null, null, null, null)
        procedures.reviewCud(call)
        call.verify()
        return 1
    }

    override fun search(instCode: String?, status: String?, keyword: String?): List<SubmissionSummary> =
        mapper.search(instCode, status, keyword?.let { "%${it.lowercase()}%" }).map {
            SubmissionSummary(
                it.subNo, it.instCode, it.regnoold, it.title, it.status, statusLabel(it.status), it.projectNo,
                it.applicantName, it.source, it.deleted, it.updatedAt, it.reviewCount, it.attachmentCount,
            )
        }

    override fun findDetail(subNo: Long): SubmissionDetail? = mapper.findDetail(subNo)?.let {
        SubmissionDetail(
            it.subNo, it.instCode, it.regnoold, it.title, it.status, statusLabel(it.status), it.projectNo, it.projectTitle,
            it.virtualProject, it.applicantUserId, it.applicantName, it.submittedAt, it.updatedAt, it.source,
            it.migrationRunId, it.deleted, it.version,
        )
    }

    override fun findReviews(subNo: Long): List<ReviewView> = mapper.findReviews(subNo).map {
        ReviewView(
            it.reviewNo, it.legacyReviewId, it.reviewerUserId, it.reviewerName, it.result,
            ReviewResult.entries.firstOrNull { result -> result.name == it.result }?.label ?: it.result,
            it.opinion, it.reviewedAt, it.source,
        )
    }

    private fun SubmissionRow.toDomain() = Submission(
        subNo = subNo,
        key = LegacySubmissionKey(instCode, regnoold),
        projectNo = projectNo,
        applicantUserId = applicantUserId,
        title = title,
        status = SubmissionStatus.valueOf(status),
        submittedAt = submittedAt,
        updatedAt = updatedAt,
        deleted = deleted,
        version = version,
    )

    private fun statusLabel(status: String): String =
        SubmissionStatus.entries.firstOrNull { it.name == status }?.label ?: status
}

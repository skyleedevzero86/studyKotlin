package com.sleekydz86.cdcmigration.submission.application

import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import org.springframework.stereotype.Service

@Service
class SubmissionQueryService(private val repository: SubmissionRepository) {

    fun search(instCode: String?, status: String?, keyword: String?): List<SubmissionSummary> =
        repository.search(instCode?.ifBlank { null }, status?.ifBlank { null }, keyword?.trim()?.ifBlank { null })

    fun detail(subNo: Long): SubmissionDetailWithReviews {
        val detail = repository.findDetail(subNo) ?: throw BusinessException(ErrorCode.SUBMISSION_NOT_FOUND, subNo.toString())
        return SubmissionDetailWithReviews(detail, repository.findReviews(subNo))
    }
}

data class SubmissionDetailWithReviews(val submission: SubmissionDetail, val reviews: List<ReviewView>)

package com.sleekydz86.cdcmigration.submission.presentation

import com.sleekydz86.cdcmigration.submission.application.SubmissionDetailWithReviews
import com.sleekydz86.cdcmigration.submission.application.SubmissionQueryService
import com.sleekydz86.cdcmigration.submission.application.SubmissionSummary
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "심의 신청")
@RestController
@RequestMapping("/api/submissions")
class SubmissionController(private val queries: SubmissionQueryService) {

    @Operation(summary = "심의 신청 목록 (기관·상태·제목 검색)")
    @GetMapping
    fun search(
        @RequestParam(required = false) instCode: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) keyword: String?,
    ): List<SubmissionSummary> = queries.search(instCode, status, keyword)

    @Operation(summary = "심의 신청 상세와 심의 이력")
    @GetMapping("/{subNo}")
    fun detail(@PathVariable subNo: Long): SubmissionDetailWithReviews = queries.detail(subNo)
}

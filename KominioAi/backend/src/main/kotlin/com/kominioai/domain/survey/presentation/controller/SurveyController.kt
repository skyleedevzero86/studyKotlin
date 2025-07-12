package com.kominioai.domain.survey.presentation.controller

import com.kominioai.domain.survey.application.model.SurveyStatus
import com.kominioai.domain.survey.application.service.SurveyApplicationService
import com.kominioai.domain.survey.presentation.dto.SurveyListResponse
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/surveys")
class SurveyController(
    private val surveyService: SurveyApplicationService
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) author: String?,
        @RequestParam(required = false) status: SurveyStatus?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Mono<SurveyListResponse> =
        surveyService.getSurveyList(title, author, status, page, size)
            .map { SurveyListResponse.from(it) }

    @DeleteMapping
    fun delete(@RequestBody ids: List<Long>): Mono<Void> =
        surveyService.deleteSurveys(ids)
}
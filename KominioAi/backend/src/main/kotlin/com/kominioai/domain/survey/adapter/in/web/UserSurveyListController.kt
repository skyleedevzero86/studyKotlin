package com.kominioai.domain.survey.adapter.`in`.web

import com.kominioai.domain.survey.application.dto.UserSurveyListQuery
import com.kominioai.domain.survey.application.port.`in`.GetUserSurveyListUseCase
import com.kominioai.domain.survey.application.dto.UserSurveyListResponse
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.LocalDate

@RestController
@RequestMapping("/api/user/surveys")
class UserSurveyListController(
    private val getUserSurveyListUseCase: GetUserSurveyListUseCase
) {
    @GetMapping
    fun getSurveyList(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false, name = "surveyType") surveyType: String?,
        @RequestParam(required = false) start: String?,
        @RequestParam(required = false) end: String?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Mono<UserSurveyListResponse> {
        val statusEnum = status?.let { runCatching { com.kominioai.domain.survey.domain.model.SurveyStatus.valueOf(it) }.getOrNull() }
        val typeEnum = surveyType?.let { runCatching { com.kominioai.domain.survey.domain.model.SurveyType.valueOf(it) }.getOrNull() }
        val startDate = start?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val endDate = end?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

        val query = UserSurveyListQuery(
            title = title,
            status = statusEnum,
            surveyType = typeEnum,
            start = startDate,
            end = endDate,
            page = page,
            size = size
        )
        return getUserSurveyListUseCase.getSurveyList(query)
    }
}
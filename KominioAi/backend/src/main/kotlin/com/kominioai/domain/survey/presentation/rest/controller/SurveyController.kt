package com.kominioai.domain.survey.presentation.rest.controller

import com.kominioai.domain.survey.application.port.input.command.AddQuestionCommand
import com.kominioai.domain.survey.application.port.input.command.CreateSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.PublishSurveyCommand
import com.kominioai.domain.survey.application.port.input.query.GetSurveyQuery
import com.kominioai.domain.survey.application.port.input.query.GetUserSurveysQuery
import com.kominioai.domain.survey.application.service.SurveyApplicationService
import com.kominioai.domain.survey.application.service.SurveyQueryService
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.presentation.rest.dto.request.AddQuestionRequest
import com.kominioai.domain.survey.presentation.rest.dto.request.CreateSurveyRequest
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyDto
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/surveys")
@Validated
class SurveyController(
    private val surveyApplicationService: SurveyApplicationService,
    private val surveyQueryService: SurveyQueryService
) {

    @PostMapping
    suspend fun createSurvey(
        @Valid @RequestBody request: CreateSurveyRequest,
        @AuthenticationPrincipal user: UserDetails
    ): ResponseEntity<SurveyDto> {
        val command = CreateSurveyCommand(
            title = request.title,
            description = request.description,
            createdBy = UserId(user.username)
        )

        val survey = surveyApplicationService.createSurvey(command)
        return ResponseEntity.ok(survey)
    }

    @PostMapping("/{surveyId}/questions")
    suspend fun addQuestion(
        @PathVariable surveyId: String,
        @Valid @RequestBody request: AddQuestionRequest
    ): ResponseEntity<SurveyDto> {
        val command = AddQuestionCommand(
            surveyId = SurveyId(surveyId),
            title = request.title,
            type = request.type,
            isRequired = request.isRequired,
            options = request.options
        )

        val survey = surveyApplicationService.addQuestion(command)
        return ResponseEntity.ok(survey)
    }

    @PostMapping("/{surveyId}/publish")
    suspend fun publishSurvey(@PathVariable surveyId: String): ResponseEntity<SurveyDto> {
        val command = PublishSurveyCommand(SurveyId(surveyId))
        val survey = surveyApplicationService.publishSurvey(command)
        return ResponseEntity.ok(survey)
    }

    @GetMapping("/{surveyId}")
    suspend fun getSurvey(@PathVariable surveyId: String): ResponseEntity<SurveyDto> {
        val query = GetSurveyQuery(SurveyId(surveyId))
        val survey = surveyQueryService.getSurvey(query)
        return survey?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }

    @GetMapping("/my")
    suspend fun getMySurveys(@AuthenticationPrincipal user: UserDetails): ResponseEntity<List<SurveyDto>> {
        val query = GetUserSurveysQuery(UserId(user.username))
        val surveys = surveyQueryService.getUserSurveys(query)
        return ResponseEntity.ok(surveys)
    }

    @GetMapping("/published")
    suspend fun getPublishedSurveys(): ResponseEntity<List<SurveyDto>> {
        val surveys = surveyQueryService.getPublishedSurveys()
        return ResponseEntity.ok(surveys)
    }
}
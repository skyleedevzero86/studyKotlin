package com.kominioai.domain.survey.presentation.rest.controller

import com.kominioai.domain.survey.application.service.*
import com.kominioai.domain.survey.application.port.input.command.*
import com.kominioai.domain.survey.application.port.input.query.*
import com.kominioai.domain.survey.domain.model.SurveySettings
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.presentation.rest.dto.common.*
import com.kominioai.domain.survey.presentation.rest.dto.request.AddQuestionRequest
import com.kominioai.domain.survey.presentation.rest.dto.request.CreateSurveyRequest
import com.kominioai.domain.survey.presentation.rest.dto.request.PublishSurveyRequest
import com.kominioai.domain.survey.presentation.rest.dto.response.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.validation.annotation.Validated
import jakarta.validation.Valid
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/surveys")
@Validated
class SurveyController(
    private val createSurveyUseCase: CreateSurveyUseCase,
    private val addQuestionUseCase: AddQuestionUseCase,
    private val publishSurveyUseCase: PublishSurveyUseCase,
    private val getSurveyUseCase: GetSurveyUseCase,
    private val getSurveyStatisticsUseCase: GetSurveyStatisticsUseCase
) {

    @PostMapping
    fun createSurvey(@Valid @RequestBody request: CreateSurveyRequest): Mono<ResponseEntity<CreateSurveyResponse>> {
        val command = CreateSurveyCommand(
            title = request.title,
            description = request.description,
            createdBy = UserId.from(request.createdBy),
            settings = SurveySettings(
                allowAnonymous = request.allowAnonymous,
                allowMultipleResponses = request.allowMultipleResponses,
                requireLogin = request.requireLogin,
                collectIpAddress = request.collectIpAddress
            )
        )
        return createSurveyUseCase.execute(command)
            .map { ResponseEntity.status(HttpStatus.CREATED).body(CreateSurveyResponse(it.value)) }
    }

    @PostMapping("/{surveyId}/questions")
    fun addQuestion(
        @PathVariable surveyId: String,
        @Valid @RequestBody request: AddQuestionRequest
    ): Mono<ResponseEntity<AddQuestionResponse>> {
        val command = AddQuestionCommand(
            surveyId = SurveyId.from(surveyId),
            order = request.order,
            text = request.text,
            description = request.description,
            type = request.type,
            required = request.required,
            options = request.options
        )
        return addQuestionUseCase.execute(command)
            .map { ResponseEntity.status(HttpStatus.CREATED).body(AddQuestionResponse(it.value)) }
    }

    @PostMapping("/{surveyId}/publish")
    fun publishSurvey(
        @PathVariable surveyId: String,
        @RequestBody request: PublishSurveyRequest
    ): Mono<ResponseEntity<Unit>> {
        val command = PublishSurveyCommand(
            surveyId = SurveyId.from(surveyId),
            userId = UserId.from(request.userId)
        )
        return publishSurveyUseCase.execute(command)
            .thenReturn(ResponseEntity.ok().build())
    }

    @GetMapping("/{surveyId}")
    fun getSurvey(@PathVariable surveyId: String): Mono<ResponseEntity<SurveyDto>> {
        val query = GetSurveyQuery(SurveyId.from(surveyId))
        return getSurveyUseCase.execute(query)
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }

    @GetMapping("/{surveyId}/statistics")
    fun getSurveyStatistics(@PathVariable surveyId: String): Mono<ResponseEntity<SurveyStatisticsDto>> {
        val query = GetSurveyStatisticsQuery(SurveyId.from(surveyId))
        return getSurveyStatisticsUseCase.execute(query)
            .map { ResponseEntity.ok(it) }
    }
}
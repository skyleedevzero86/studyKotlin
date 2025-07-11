package com.kominioai.domain.survey.presentation.rest.controller

import com.kominioai.domain.survey.application.port.input.SurveyUseCase
import com.kominioai.domain.survey.application.port.input.command.AddQuestionCommand
import com.kominioai.domain.survey.application.port.input.command.CreateSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.SubmitResponseCommand
import com.kominioai.domain.survey.application.port.input.query.GetSurveyQuery
import com.kominioai.domain.survey.application.port.input.query.GetUserSurveysQuery
import com.kominioai.domain.survey.application.port.input.query.SurveyStatistics
import com.kominioai.domain.survey.application.service.SurveyApplicationService
import com.kominioai.domain.survey.application.service.SurveyQueryService
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.presentation.rest.dto.request.AddQuestionRequest
import com.kominioai.domain.survey.presentation.rest.dto.request.CreateSurveyRequest
import com.kominioai.domain.survey.presentation.rest.dto.request.SubmitResponseRequest
import com.kominioai.domain.survey.presentation.rest.dto.response.ResponseSubmissionResult
import com.kominioai.domain.survey.presentation.rest.dto.common.SurveyDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/surveys")
@Validated
class SurveyController(
    private val surveyApplicationService: SurveyApplicationService,
    private val surveyQueryService: SurveyQueryService,
    private val surveyUseCase: SurveyUseCase
) {

    @PostMapping
    fun createSurvey(
        @Valid @RequestBody request: CreateSurveyRequest,
        @AuthenticationPrincipal user: UserDetails
    ): Mono<ResponseEntity<SurveyDto>> {
        val command = CreateSurveyCommand(
            title = request.title,
            description = request.description,
            createdBy = UserId(user.username)
        )

        return surveyApplicationService.createSurvey(command)
            .map { ResponseEntity.ok(it) }
    }

    @PostMapping("/{surveyId}/questions")
    fun addQuestion(
        @PathVariable surveyId: String,
        @Valid @RequestBody request: AddQuestionRequest
    ): Mono<ResponseEntity<SurveyDto>> {
        val command = AddQuestionCommand(
            surveyId = SurveyId(surveyId),
            title = request.title,
            type = request.type,
            isRequired = request.isRequired,
            options = request.options
        )

        return surveyApplicationService.addQuestion(command)
            .map { ResponseEntity.ok(it) }
    }

    @PutMapping("/{surveyId}/activate")
    fun activateSurvey(@PathVariable surveyId: String): Mono<ResponseEntity<SurveyDto>> {
        return surveyUseCase.activateSurvey(UUID.fromString(surveyId))
            .map { ResponseEntity.ok(it) }
    }

    @GetMapping("/{surveyId}")
    fun getSurvey(@PathVariable surveyId: String): Mono<ResponseEntity<SurveyDto>> {
        return surveyUseCase.getSurvey(UUID.fromString(surveyId))
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }

    @GetMapping("/my")
    fun getMySurveys(@AuthenticationPrincipal user: UserDetails): Mono<ResponseEntity<Flux<SurveyDto>>> {
        val query = GetUserSurveysQuery(UserId(user.username))
        return Mono.just(ResponseEntity.ok(surveyQueryService.getUserSurveys(query)))
    }

    @GetMapping("/published")
    fun getPublishedSurveys(): Mono<ResponseEntity<Flux<SurveyDto>>> {
        return Mono.just(ResponseEntity.ok(surveyQueryService.getPublishedSurveys()))
    }

    @PostMapping("/{surveyId}/responses")
    @ResponseStatus(HttpStatus.CREATED)
    fun submitResponse(
        @PathVariable surveyId: String,
        @Valid @RequestBody request: SubmitResponseRequest,
        @AuthenticationPrincipal user: UserDetails?
    ): Mono<ResponseEntity<ResponseSubmissionResult>> {
        val command = SubmitResponseCommand(
            surveyId = SurveyId(surveyId),
            respondentId = user?.username?.let { UserId(it) },
            answers = request.answers
        )
        return surveyUseCase.submitResponse(command)
            .map { ResponseSubmissionResult(it) }
            .map { ResponseEntity.ok(it) }
    }

    @GetMapping
    fun getAllSurveys(): Flux<SurveyDto> {
        return surveyUseCase.getAllSurveys()
    }

    @GetMapping("/{surveyId}/statistics")
    fun getSurveyStatistics(@PathVariable surveyId: String): Mono<ResponseEntity<SurveyStatistics>> {
        return surveyUseCase.getSurveyStatistics(UUID.fromString(surveyId))
            .map { ResponseEntity.ok(it) }
    }
}
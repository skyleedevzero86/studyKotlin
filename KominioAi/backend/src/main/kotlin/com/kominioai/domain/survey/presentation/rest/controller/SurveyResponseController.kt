package com.kominioai.domain.survey.presentation.rest.controller

import com.kominioai.domain.survey.application.port.input.command.SubmitResponseCommand
import com.kominioai.domain.survey.application.port.input.query.GetSurveyResponsesQuery
import com.kominioai.domain.survey.application.service.SurveyApplicationService
import com.kominioai.domain.survey.application.service.SurveyQueryService
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.presentation.rest.dto.request.SubmitResponseRequest
import com.kominioai.domain.survey.presentation.rest.dto.response.ResponseSubmissionResult
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyResponseDto
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/responses")
class SurveyResponseController(
    private val surveyApplicationService: SurveyApplicationService,
    private val surveyQueryService: SurveyQueryService
) {

    @PostMapping
    fun submitResponse(
        @Valid @RequestBody request: SubmitResponseRequest,
        @AuthenticationPrincipal user: UserDetails?
    ): Mono<ResponseEntity<ResponseSubmissionResult>> {
        val answers = request.answers.map { answerSubmission ->
            com.kominioai.domain.survey.domain.model.domain.Answer.create(
                responseId = "",
                questionId = com.kominioai.domain.survey.domain.valueobject.QuestionId.from(answerSubmission.questionId),
                questionType = com.kominioai.domain.survey.domain.valueobject.QuestionType.TEXT,
                textAnswer = answerSubmission.answerText,
                selectedOptions = emptyList()
            )
        }

        val command = SubmitResponseCommand(
            surveyId = SurveyId.from(request.surveyId),
            respondentId = user?.username,
            answers = answers,
            ipAddress = null
        )

        return surveyApplicationService.submitResponse(command)
            .map { ResponseSubmissionResult(it.value) }
            .map { ResponseEntity.ok(it) }
    }

    @GetMapping("/survey/{surveyId}")
    fun getSurveyResponses(@PathVariable surveyId: String): Mono<ResponseEntity<Flux<SurveyResponseDto>>> {
        val query = GetSurveyResponsesQuery(SurveyId.from(surveyId))
        val responses = surveyQueryService.getSurveyResponses(query)
        return Mono.just(ResponseEntity.ok(responses))
    }
}
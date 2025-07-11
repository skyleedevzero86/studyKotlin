package com.kominioai.domain.survey.presentation.rest.controller

import com.kominioai.domain.survey.application.port.input.command.SubmitResponseCommand
import com.kominioai.domain.survey.application.port.input.query.GetSurveyResponsesQuery
import com.kominioai.domain.survey.application.service.SurveyApplicationService
import com.kominioai.domain.survey.application.service.SurveyQueryService
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.presentation.rest.dto.request.SubmitResponseRequest
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyResponseDto
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/responses")
class SurveyResponseController(
    private val surveyApplicationService: SurveyApplicationService,
    private val surveyQueryService: SurveyQueryService
) {

    @PostMapping
    suspend fun submitResponse(
        @Valid @RequestBody request: SubmitResponseRequest,
        @AuthenticationPrincipal user: UserDetails?
    ): ResponseEntity<SurveyResponseDto> {
        val command = SubmitResponseCommand(
            surveyId = SurveyId(request.surveyId),
            respondentId = user?.username?.let { UserId(it) },
            answers = request.answers
        )

        val response = surveyApplicationService.submitResponse(command)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/survey/{surveyId}")
    suspend fun getSurveyResponses(@PathVariable surveyId: String): ResponseEntity<List<SurveyResponseDto>> {
        val query = GetSurveyResponsesQuery(SurveyId(surveyId))
        val responses = surveyQueryService.getSurveyResponses(query)
        return ResponseEntity.ok(responses)
    }
}
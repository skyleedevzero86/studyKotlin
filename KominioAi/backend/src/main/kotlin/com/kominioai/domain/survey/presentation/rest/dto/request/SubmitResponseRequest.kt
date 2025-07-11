package com.kominioai.domain.survey.presentation.rest.dto.request

import com.kominioai.domain.survey.application.port.input.command.AnswerSubmission
import jakarta.validation.constraints.NotBlank

data class SubmitResponseRequest(
    @field:NotBlank val surveyId: String,
    val answers: List<AnswerSubmission>
)
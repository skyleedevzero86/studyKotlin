package com.kochat.adapter.inbound.web.survey.dto

import com.kochat.domain.survey.model.QuestionType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SurveyQuestionRequest(
    @field:NotBlank @field:Size(max = 500)
    val questionText: String,
    val questionType: QuestionType = QuestionType.SINGLE_CHOICE,
    @field:Valid
    val options: List<SurveyOptionRequest> = emptyList(),
)

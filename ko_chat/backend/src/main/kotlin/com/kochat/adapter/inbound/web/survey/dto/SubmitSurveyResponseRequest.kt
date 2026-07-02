package com.kochat.adapter.inbound.web.survey.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

data class SubmitSurveyResponseRequest(
    @field:NotEmpty
    @field:Valid
    val answers: List<SurveyAnswerItemRequest>,
)

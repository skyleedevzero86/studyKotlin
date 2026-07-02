package com.kochat.adapter.inbound.web.survey.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SurveyOptionRequest(
    @field:NotBlank @field:Size(max = 300)
    val optionText: String,
)

package com.kominioai.domain.survey.presentation.rest.dto.request

import jakarta.validation.constraints.NotBlank

data class CreateSurveyRequest(
    @field:NotBlank val title: String,
    val description: String?
)
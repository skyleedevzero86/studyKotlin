package com.kominioai.domain.survey.presentation.rest.dto.request

import jakarta.validation.constraints.NotBlank

data class PublishSurveyRequest(
    @field:NotBlank val userId: String
)
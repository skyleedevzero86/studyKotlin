package com.kominioai.domain.survey.presentation.rest.dto.request

import jakarta.validation.constraints.NotBlank

data class CreateSurveyRequest(
    @field:NotBlank val title: String,
    val description: String?,
    @field:NotBlank val createdBy: String,
    val allowAnonymous: Boolean = true,
    val allowMultipleResponses: Boolean = false,
    val requireLogin: Boolean = false,
    val collectIpAddress: Boolean = false
)
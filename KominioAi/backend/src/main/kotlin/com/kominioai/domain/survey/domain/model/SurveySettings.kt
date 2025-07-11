package com.kominioai.domain.survey.domain.model

data class SurveySettings(
    val allowAnonymous: Boolean = true,
    val allowMultipleResponses: Boolean = false,
    val requireLogin: Boolean = false,
    val collectIpAddress: Boolean = false
)
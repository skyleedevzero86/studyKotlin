package com.kominioai.domain.survey.application.port.input.query

import com.kominioai.domain.survey.domain.valueobject.SurveyStatus

data class SurveyStatistics(
    val surveyId: String,
    val title: String,
    val totalResponses: Long,
    val status: SurveyStatus
)
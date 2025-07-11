package com.kominioai.domain.survey.application.port.input.query

import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import java.util.UUID

data class SurveyStatistics(
    val surveyId: UUID,
    val title: String,
    val totalResponses: Long,
    val status: SurveyStatus
)
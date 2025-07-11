package com.kominioai.domain.survey.presentation.rest.dto.response

import com.kominioai.domain.survey.domain.valueobject.SurveyId

data class SurveyStatisticsDto(
    val surveyId: SurveyId,
    val title: String,
    val responseCount: Int,
    val questionStatistics: List<QuestionStatisticsDto>
)
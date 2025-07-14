package com.kominioai.domain.survey.application.query

data class SurveyResultQuery(
    val surveyId: String,
    val userId: String? = null
)
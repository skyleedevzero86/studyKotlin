package com.kominioai.domain.survey.application.query

data class SurveyDetailQuery(
    val surveyId: Long,
    val userId: String,
    val includeQuestions: Boolean = true
)
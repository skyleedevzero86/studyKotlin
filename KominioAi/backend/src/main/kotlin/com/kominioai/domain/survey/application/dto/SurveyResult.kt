package com.kominioai.domain.survey.application.dto

data class SurveyResult(
    val questionOrder: Int,
    val questionContent: String,
    val answer: String
)
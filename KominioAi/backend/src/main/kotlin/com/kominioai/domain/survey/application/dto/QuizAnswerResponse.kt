package com.kominioai.domain.survey.application.dto

data class QuizAnswerResponse(
    val questionId: String,
    val answerType: String,
    val answerContent: Any
)
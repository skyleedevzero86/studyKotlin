package com.kominioai.domain.survey.application.port.input.command


data class AnswerSubmission(
    val questionId: String,
    val answerText: String?,
    val selectedOptionIds: List<String> = emptyList()
)
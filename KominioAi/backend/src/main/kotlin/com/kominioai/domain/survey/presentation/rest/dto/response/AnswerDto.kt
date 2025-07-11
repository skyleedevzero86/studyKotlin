package com.kominioai.domain.survey.presentation.rest.dto.response

data class AnswerDto(
    val id: String,
    val questionId: String,
    val answerText: String?,
    val selectedOptionId: String?
)
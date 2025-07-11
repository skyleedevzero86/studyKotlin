package com.kominioai.domain.survey.presentation.rest.dto.common

data class QuestionOptionDto(
    val id: String,
    val text: String,
    val orderIndex: Int
)
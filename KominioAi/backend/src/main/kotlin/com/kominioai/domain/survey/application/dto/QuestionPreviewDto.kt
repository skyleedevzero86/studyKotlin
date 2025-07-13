package com.kominioai.domain.survey.application.dto


data class QuestionPreviewDto(
    val number: Int,
    val content: String,
    val type: String,
    val icon: String,
    val required: Boolean
)
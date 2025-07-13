package com.kominioai.domain.survey.domain.model

data class QuestionOption(
    val id: Long? = null,
    val content: String,
    val order: Int
)
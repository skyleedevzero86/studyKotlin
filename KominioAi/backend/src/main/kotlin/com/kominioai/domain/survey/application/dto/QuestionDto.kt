package com.kominioai.domain.survey.application.dto

import com.kominioai.domain.survey.domain.model.QuestionType

data class QuestionDto(
    val content: String,
    val type: QuestionType,
    val order: Int,
    val options: List<String>?
)
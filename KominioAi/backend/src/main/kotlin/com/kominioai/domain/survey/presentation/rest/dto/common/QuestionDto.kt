package com.kominioai.domain.survey.presentation.rest.dto.common

import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.presentation.rest.dto.common.QuestionOptionDto

data class QuestionDto(
    val id: String,
    val title: String,
    val type: QuestionType,
    val isRequired: Boolean,
    val orderIndex: Int,
    val options: List<QuestionOptionDto> = emptyList()
)
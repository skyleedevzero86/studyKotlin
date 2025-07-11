package com.kominioai.domain.survey.presentation.rest.dto.request

import com.kominioai.domain.survey.domain.valueobject.QuestionType
import jakarta.validation.constraints.NotBlank

data class AddQuestionRequest(
    @field:NotBlank val title: String,
    val type: QuestionType,
    val isRequired: Boolean = false,
    val options: List<String> = emptyList()
)
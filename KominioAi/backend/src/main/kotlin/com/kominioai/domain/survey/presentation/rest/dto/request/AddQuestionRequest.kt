package com.kominioai.domain.survey.presentation.rest.dto.request

import com.kominioai.domain.survey.domain.valueobject.QuestionType
import jakarta.validation.constraints.NotBlank

data class AddQuestionRequest(
    @field:NotBlank val text: String,
    val description: String?,
    val type: QuestionType,
    val required: Boolean = false,
    val order: Int,
    val options: List<String> = emptyList()
)
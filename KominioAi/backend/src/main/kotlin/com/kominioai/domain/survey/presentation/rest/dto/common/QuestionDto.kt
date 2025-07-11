package com.kominioai.domain.survey.presentation.rest.dto.common

import com.kominioai.domain.survey.domain.model.domain.Question
import com.kominioai.domain.survey.domain.valueobject.QuestionType

data class QuestionDto(
    val id: String,
    val order: Int,
    val text: String,
    val description: String?,
    val type: QuestionType,
    val required: Boolean,
    val options: List<QuestionOptionDto>
) {
    companion object {
        fun from(question: Question): QuestionDto {
            return QuestionDto(
                id = question.id.value,
                order = question.order,
                text = question.text,
                description = question.description,
                type = question.type,
                required = question.required,
                options = question.options.map { QuestionOptionDto.from(it) }
            )
        }
    }
}
package com.kominioai.domain.survey.presentation.rest.dto.common

import com.kominioai.domain.survey.domain.model.domain.QuestionOption

data class QuestionOptionDto(
    val id: String,
    val order: Int,
    val text: String
) {
    companion object {
        fun from(option: QuestionOption): QuestionOptionDto {
            return QuestionOptionDto(
                id = option.id.value,
                order = option.order,
                text = option.text
            )
        }
    }
}
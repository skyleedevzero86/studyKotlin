package com.kominioai.domain.survey.domain.model

import com.kominioai.domain.survey.domain.valueobject.QuestionOptionId

data class QuestionOption(
    val id: QuestionOptionId,
    val order: Int,
    val text: String
) {
    companion object {
        fun create(
            order: Int,
            text: String
        ): QuestionOption {
            return QuestionOption(
                id = QuestionOptionId.generate(),
                order = order,
                text = text
            )
        }
    }
}
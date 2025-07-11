package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity

import com.kominioai.domain.survey.domain.model.domain.QuestionOption as DomainQuestionOption
import com.kominioai.domain.survey.domain.valueobject.QuestionOptionId
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("question_options")
data class QuestionOption(
    @Id
    val id: String,
    val questionId: String,
    val orderIndex: Int,
    val text: String
) {
    fun toDomain(): DomainQuestionOption {
        return DomainQuestionOption(
            id = QuestionOptionId.from(id),
            order = orderIndex,
            text = text
        )
    }

    companion object {
        fun from(option: DomainQuestionOption, questionId: String): QuestionOption {
            return QuestionOption(
                id = option.id.value,
                questionId = questionId,
                orderIndex = option.order,
                text = option.text
            )
        }
    }
}
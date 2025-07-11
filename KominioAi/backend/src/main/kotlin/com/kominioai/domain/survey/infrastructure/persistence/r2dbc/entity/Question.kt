package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity

import com.kominioai.domain.survey.domain.model.domain.Question as DomainQuestion
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("questions")
data class Question(
    @Id
    val id: String,
    val surveyId: String,
    val orderIndex: Int,
    val text: String,
    val description: String?,
    val type: QuestionType,
    val required: Boolean
) {
    fun toDomain(): DomainQuestion {
        return DomainQuestion(
            id = QuestionId.from(id),
            surveyId = SurveyId.from(surveyId),
            order = orderIndex,
            text = text,
            description = description,
            type = type,
            required = required,
            options = emptyList()
        )
    }

    companion object {
        fun from(question: DomainQuestion): Question {
            return Question(
                id = question.id.value,
                surveyId = question.surveyId.value,
                orderIndex = question.order,
                text = question.text,
                description = question.description,
                type = question.type,
                required = question.required
            )
        }
    }
}

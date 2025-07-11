package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity

import com.kominioai.domain.survey.domain.model.domain.Answer as DomainAnswer
import com.kominioai.domain.survey.domain.valueobject.AnswerId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("answers")
data class Answer(
    @Id
    val id: String,
    val responseId: String,
    val questionId: String,
    val questionType: QuestionType,
    val textAnswer: String?,
    val createdAt: LocalDateTime
) {
    fun toDomain(): DomainAnswer {
        return DomainAnswer(
            id = AnswerId.from(id),
            responseId = responseId,
            questionId = QuestionId.from(questionId),
            questionType = questionType,
            textAnswer = textAnswer,
            selectedOptions = emptyList(),
            createdAt = createdAt
        )
    }

    companion object {
        fun from(answer: DomainAnswer): Answer {
            return Answer(
                id = answer.id.value,
                responseId = answer.responseId,
                questionId = answer.questionId.value,
                questionType = answer.questionType,
                textAnswer = answer.textAnswer,
                createdAt = answer.createdAt
            )
        }
    }
}

package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity

import com.kominioai.domain.survey.domain.valueobject.AnswerId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("response_answers")
data class ResponseAnswer(
    @Id
    val id: String,
    val surveyResponseId: String,
    val questionId: String,
    val questionType: QuestionType,
    val textAnswer: String?,
    val selectedOptionIds: String?, // JSON 형태로 저장
    val createdAt: LocalDateTime
) {
    fun toDomain(): com.kominioai.domain.survey.domain.model.domain.Answer {
        return com.kominioai.domain.survey.domain.model.domain.Answer(
            id = AnswerId.from(id),
            responseId = surveyResponseId,
            questionId = QuestionId.from(questionId),
            questionType = questionType,
            textAnswer = textAnswer,
            selectedOptions = parseSelectedOptions(),
            createdAt = createdAt
        )
    }

    private fun parseSelectedOptions(): List<com.kominioai.domain.survey.domain.model.domain.QuestionOption> {
        return if (selectedOptionIds.isNullOrBlank()) {
            emptyList()
        } else {
            emptyList()
        }
    }

    companion object {
        fun from(domainAnswer: com.kominioai.domain.survey.domain.model.domain.Answer, surveyResponseId: String): ResponseAnswer {
            return ResponseAnswer(
                id = domainAnswer.id.value,
                surveyResponseId = surveyResponseId,
                questionId = domainAnswer.questionId.value,
                questionType = domainAnswer.questionType,
                textAnswer = domainAnswer.textAnswer,
                selectedOptionIds = serializeSelectedOptions(domainAnswer.selectedOptions),
                createdAt = domainAnswer.createdAt
            )
        }

        private fun serializeSelectedOptions(options: List<com.kominioai.domain.survey.domain.model.domain.QuestionOption>): String? {
            return if (options.isEmpty()) null else options.joinToString(",") { it.id.value }
        }
    }

    fun toDomainWithSelectedOptions(selectedOptions: List<com.kominioai.domain.survey.domain.model.domain.QuestionOption>): com.kominioai.domain.survey.domain.model.domain.Answer {
        return com.kominioai.domain.survey.domain.model.domain.Answer(
            id = AnswerId.from(id),
            responseId = surveyResponseId,
            questionId = QuestionId.from(questionId),
            questionType = questionType,
            textAnswer = textAnswer,
            selectedOptions = selectedOptions,
            createdAt = createdAt
        )
    }
}
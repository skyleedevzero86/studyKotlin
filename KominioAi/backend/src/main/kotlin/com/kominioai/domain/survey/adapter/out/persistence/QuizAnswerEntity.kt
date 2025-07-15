package com.kominioai.domain.survey.adapter.out.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("quiz_answers")
data class QuizAnswerEntity(
    @Id
    val id: String? = null,
    val participationId: String,
    val questionId: String,
    val answerType: String,
    val answerContent: String,
    val submittedAt: LocalDateTime
) {
    companion object {
        fun fromDomain(answer: com.kominioai.domain.survey.domain.model.QuizAnswer, participationId: String): QuizAnswerEntity {
            val (content, type) = when (answer) {
                is com.kominioai.domain.survey.domain.model.QuizAnswer.SingleChoice ->
                    answer.selectedOptionId.value to "SINGLE_CHOICE"
                is com.kominioai.domain.survey.domain.model.QuizAnswer.MultipleChoice ->
                    answer.selectedOptionIds.joinToString(",") { it.value } to "MULTIPLE_CHOICE"
                is com.kominioai.domain.survey.domain.model.QuizAnswer.ShortText ->
                    answer.text to "SHORT_TEXT"
                is com.kominioai.domain.survey.domain.model.QuizAnswer.LongText ->
                    answer.text to "LONG_TEXT"
            }

            return QuizAnswerEntity(
                id = answer.id.value,
                participationId = participationId,
                questionId = answer.questionId.value,
                answerType = type,
                answerContent = content,
                submittedAt = answer.submittedAt
            )
        }
    }

    fun toDomain(): com.kominioai.domain.survey.domain.model.QuizAnswer {
        return when (answerType) {
            "SINGLE_CHOICE" -> com.kominioai.domain.survey.domain.model.QuizAnswer.createSingleChoice(
                questionId = com.kominioai.domain.survey.domain.model.QuestionId.fromString(questionId),
                selectedOptionId = com.kominioai.domain.survey.domain.model.QuestionOptionId.fromString(answerContent)
            )
            "MULTIPLE_CHOICE" -> com.kominioai.domain.survey.domain.model.QuizAnswer.createMultipleChoice(
                questionId = com.kominioai.domain.survey.domain.model.QuestionId.fromString(questionId),
                selectedOptionIds = answerContent.split(",").map {
                    com.kominioai.domain.survey.domain.model.QuestionOptionId.fromString(it)
                }
            )
            "SHORT_TEXT" -> com.kominioai.domain.survey.domain.model.QuizAnswer.createShortText(
                questionId = com.kominioai.domain.survey.domain.model.QuestionId.fromString(questionId),
                text = answerContent
            )
            "LONG_TEXT" -> com.kominioai.domain.survey.domain.model.QuizAnswer.createLongText(
                questionId = com.kominioai.domain.survey.domain.model.QuestionId.fromString(questionId),
                text = answerContent
            )
            else -> throw IllegalArgumentException("Unknown answer type: $answerType")
        }
    }
}
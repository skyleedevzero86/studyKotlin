package com.kominioai.domain.survey.domain.model.domain

import com.kominioai.domain.survey.domain.valueobject.AnswerId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import java.time.LocalDateTime

data class Answer(
    val id: AnswerId,
    val responseId: String,
    val questionId: QuestionId,
    val questionType: QuestionType,
    val textAnswer: String?,
    val selectedOptions: List<QuestionOption>,
    val createdAt: LocalDateTime
) {
    companion object {
        fun create(
            responseId: String,
            questionId: QuestionId,
            questionType: QuestionType,
            textAnswer: String?,
            selectedOptions: List<QuestionOption>
        ): Answer {
            return Answer(
                id = AnswerId.generate(),
                responseId = responseId,
                questionId = questionId,
                questionType = questionType,
                textAnswer = textAnswer,
                selectedOptions = selectedOptions,
                createdAt = LocalDateTime.now()
            )
        }
    }
}
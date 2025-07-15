package com.kominioai.domain.survey.domain.model

import java.time.LocalDateTime

sealed class QuizAnswer {
    abstract val id: QuizAnswerId
    abstract val questionId: QuestionId
    abstract val submittedAt: LocalDateTime

    data class SingleChoice(
        val selectedOptionId: QuestionOptionId,
        override val id: QuizAnswerId,
        override val questionId: QuestionId,
        override val submittedAt: LocalDateTime
    ) : QuizAnswer()

    data class MultipleChoice(
        val selectedOptionIds: List<QuestionOptionId>,
        override val id: QuizAnswerId,
        override val questionId: QuestionId,
        override val submittedAt: LocalDateTime
    ) : QuizAnswer()

    data class ShortText(
        val text: String,
        override val id: QuizAnswerId,
        override val questionId: QuestionId,
        override val submittedAt: LocalDateTime
    ) : QuizAnswer()

    data class LongText(
        val text: String,
        override val id: QuizAnswerId,
        override val questionId: QuestionId,
        override val submittedAt: LocalDateTime
    ) : QuizAnswer()

    companion object {
        fun createSingleChoice(
            questionId: QuestionId,
            selectedOptionId: QuestionOptionId
        ): SingleChoice {
            return SingleChoice(
                selectedOptionId = selectedOptionId,
                id = QuizAnswerId.generate(),
                questionId = questionId,
                submittedAt = LocalDateTime.now()
            )
        }

        fun createMultipleChoice(
            questionId: QuestionId,
            selectedOptionIds: List<QuestionOptionId>
        ): MultipleChoice {
            require(selectedOptionIds.isNotEmpty()) { "최소 하나의 옵션을 선택해야 합니다." }

            return MultipleChoice(
                selectedOptionIds = selectedOptionIds,
                id = QuizAnswerId.generate(),
                questionId = questionId,
                submittedAt = LocalDateTime.now()
            )
        }

        fun createShortText(
            questionId: QuestionId,
            text: String
        ): ShortText {
            require(text.isNotBlank()) { "답변 내용은 비어있을 수 없습니다." }
            require(text.length <= 100) { "답변은 100자를 초과할 수 없습니다." }

            return ShortText(
                text = text.trim(),
                id = QuizAnswerId.generate(),
                questionId = questionId,
                submittedAt = LocalDateTime.now()
            )
        }

        fun createLongText(
            questionId: QuestionId,
            text: String
        ): LongText {
            require(text.isNotBlank()) { "답변 내용은 비어있을 수 없습니다." }
            require(text.length <= 1000) { "답변은 1000자를 초과할 수 없습니다." }

            return LongText(
                text = text.trim(),
                id = QuizAnswerId.generate(),
                questionId = questionId,
                submittedAt = LocalDateTime.now()
            )
        }
    }
}
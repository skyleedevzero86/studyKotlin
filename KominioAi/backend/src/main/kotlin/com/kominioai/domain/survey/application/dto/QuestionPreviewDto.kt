package com.kominioai.domain.survey.application.dto

import com.kominioai.domain.survey.domain.model.Question
import com.kominioai.domain.survey.domain.model.QuestionType

data class QuestionPreviewDto(
    val number: Int,
    val content: String,
    val type: String,
    val icon: String,
    val required: Boolean
) {
    companion object {
        fun from(question: Question, number: Int = 1): QuestionPreviewDto {
            return QuestionPreviewDto(
                number = number,
                content = question.getContent(),
                type = question.type.name,
                icon = when (question.type) {
                    QuestionType.MULTIPLE_CHOICE, QuestionType.QUIZ_MULTIPLE_CHOICE -> "☑️"
                    QuestionType.ESSAY, QuestionType.QUIZ_ESSAY -> "✏️"
                    QuestionType.SHORT_ANSWER, QuestionType.QUIZ_SHORT_ANSWER -> "💬"
                },
                required = question.isRequired()
            )
        }
    }
}
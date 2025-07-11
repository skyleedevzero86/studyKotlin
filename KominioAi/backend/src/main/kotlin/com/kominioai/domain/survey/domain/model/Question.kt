package com.kominioai.domain.survey.domain.model

import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.SurveyId

data class Question(
    val id: QuestionId,
    val surveyId: SurveyId,
    val order: Int,
    val text: String,
    val description: String?,
    val type: QuestionType,
    val required: Boolean,
    val options: List<QuestionOption>
) {
    companion object {
        fun create(
            surveyId: SurveyId,
            order: Int,
            text: String,
            description: String?,
            type: QuestionType,
            required: Boolean,
            options: List<String>
        ): Question {
            return Question(
                id = QuestionId.generate(),
                surveyId = surveyId,
                order = order,
                text = text,
                description = description,
                type = type,
                required = required,
                options = options.mapIndexed { index, optionText ->
                    QuestionOption.create(
                        order = index + 1,
                        text = optionText
                    )
                }
            )
        }
    }

    fun addOption(option: QuestionOption): Question {
        return copy(options = options + option)
    }

    fun validateAnswer(answer: Answer): Boolean {
        return when (type) {
            QuestionType.TEXT, QuestionType.TEXTAREA, QuestionType.NUMBER, QuestionType.DATE, QuestionType.EMAIL -> {
                answer.textAnswer?.isNotBlank() == true
            }
            QuestionType.SINGLE_CHOICE -> {
                answer.selectedOptions.size == 1
            }
            QuestionType.MULTIPLE_CHOICE -> {
                answer.selectedOptions.isNotEmpty()
            }
            QuestionType.RATING -> {
                answer.selectedOptions.size == 1
            }
            else -> true
        }
    }
}
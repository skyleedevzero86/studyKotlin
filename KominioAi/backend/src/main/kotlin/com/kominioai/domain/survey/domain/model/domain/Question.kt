package com.kominioai.domain.survey.domain.model.domain

import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.model.service.QuestionValidationService
import com.kominioai.global.exception.QuestionValidationException

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
            options: List<String>,
            validationService: QuestionValidationService? = null
        ): Question {

            validationService?.let { service ->
                service.validateQuestionCreation(
                    questionText = text,
                    description = description,
                    questionType = type,
                    isRequired = required,
                    options = options,
                    currentQuestionCount = 0
                )
            }

            validateBasicRules(text, type, options)
            
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

        private fun validateBasicRules(text: String, type: QuestionType, options: List<String>) {

            if (text.isBlank()) {
                throw QuestionValidationException("질문 텍스트는 비어있을 수 없습니다.")
            }

            when {
                type.supportsOptions() && options.isEmpty() -> {
                    throw QuestionValidationException("${type.name} 유형의 질문에는 옵션이 필요합니다.")
                }
                !type.supportsOptions() && options.isNotEmpty() -> {
                    throw QuestionValidationException("${type.name} 유형의 질문에는 옵션을 설정할 수 없습니다.")
                }
            }
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

    fun validateModification(
        newText: String,
        newDescription: String?,
        newType: QuestionType,
        newRequired: Boolean,
        newOptions: List<String>,
        validationService: QuestionValidationService
    ) {
        validationService.validateQuestionCreation(
            questionText = newText,
            description = newDescription,
            questionType = newType,
            isRequired = newRequired,
            options = newOptions,
            currentQuestionCount = 0
        )

        validationService.validateQuestionModification(
            existingQuestionType = this.type,
            newQuestionType = newType,
            existingOptions = this.options.map { it.text },
            newOptions = newOptions
        )
    }

    fun isValid(): Boolean {
        return try {
            validateBasicRules(text, type, options.map { it.text })
            true
        } catch (e: QuestionValidationException) {
            false
        }
    }
}
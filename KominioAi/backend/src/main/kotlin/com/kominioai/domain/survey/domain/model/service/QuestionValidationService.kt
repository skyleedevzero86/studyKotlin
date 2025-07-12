package com.kominioai.domain.survey.domain.model.service

import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.global.exception.QuestionValidationException
import com.kominioai.global.exception.QuestionOptionValidationException
import org.springframework.stereotype.Component

@Component
class QuestionValidationService {

    companion object {
        // 질문 텍스트 길이 제한
        const val MIN_QUESTION_TEXT_LENGTH = 1
        const val MAX_QUESTION_TEXT_LENGTH = 500

        // 질문 설명 길이 제한
        const val MAX_QUESTION_DESCRIPTION_LENGTH = 1000

        // 선택형 질문 옵션 개수 제한
        const val MIN_OPTIONS_COUNT = 2
        const val MAX_OPTIONS_COUNT = 10

        // 옵션 텍스트 길이 제한
        const val MIN_OPTION_TEXT_LENGTH = 1
        const val MAX_OPTION_TEXT_LENGTH = 200

        // 설문당 최대 질문 개수
        const val MAX_QUESTIONS_PER_SURVEY = 50
    }

    fun validateQuestionCreation(
        questionText: String,
        description: String?,
        questionType: QuestionType,
        isRequired: Boolean,
        options: List<String>,
        currentQuestionCount: Int
    ) {
        // 1. 질문 텍스트 검증
        validateQuestionText(questionText)

        // 2. 질문 설명 검증
        validateQuestionDescription(description)

        // 3. 질문 유형별 옵션 검증
        validateOptionsByQuestionType(questionType, options)

        // 4. 필수 질문 규칙 검증
        validateRequiredQuestionRules(questionType, isRequired)

        // 5. 설문당 질문 개수 제한 검증
        validateQuestionCountLimit(currentQuestionCount)
    }

    private fun validateQuestionText(text: String) {
        when {
            text.isBlank() -> {
                throw QuestionValidationException(
                    "질문 텍스트는 비어있을 수 없습니다."
                )
            }
            text.length < MIN_QUESTION_TEXT_LENGTH -> {
                throw QuestionValidationException(
                    "질문 텍스트는 최소 ${MIN_QUESTION_TEXT_LENGTH}자 이상이어야 합니다. (현재: ${text.length}자)"
                )
            }
            text.length > MAX_QUESTION_TEXT_LENGTH -> {
                throw QuestionValidationException(
                    "질문 텍스트는 최대 ${MAX_QUESTION_TEXT_LENGTH}자까지 입력 가능합니다. (현재: ${text.length}자)"
                )
            }
        }
    }

    private fun validateQuestionDescription(description: String?) {
        description?.let { desc ->
            if (desc.length > MAX_QUESTION_DESCRIPTION_LENGTH) {
                throw QuestionValidationException(
                    "질문 설명은 최대 ${MAX_QUESTION_DESCRIPTION_LENGTH}자까지 입력 가능합니다. (현재: ${desc.length}자)"
                )
            }
        }
    }

    private fun validateOptionsByQuestionType(questionType: QuestionType, options: List<String>) {
        when {

            questionType.supportsOptions() -> {
                validateChoiceQuestionOptions(options)
            }

            else -> {
                if (options.isNotEmpty()) {
                    throw QuestionValidationException(
                        "${questionType.name} 유형의 질문에는 옵션을 설정할 수 없습니다."
                    )
                }
            }
        }
    }

    private fun validateChoiceQuestionOptions(options: List<String>) {
        when {
            options.isEmpty() -> {
                throw QuestionOptionValidationException(
                    "선택형 질문에는 최소 ${MIN_OPTIONS_COUNT}개 이상의 옵션이 필요합니다."
                )
            }
            options.size < MIN_OPTIONS_COUNT -> {
                throw QuestionOptionValidationException(
                    "선택형 질문에는 최소 ${MIN_OPTIONS_COUNT}개 이상의 옵션이 필요합니다. (현재: ${options.size}개)"
                )
            }
            options.size > MAX_OPTIONS_COUNT -> {
                throw QuestionOptionValidationException(
                    "선택형 질문의 옵션은 최대 ${MAX_OPTIONS_COUNT}개까지 설정 가능합니다. (현재: ${options.size}개)"
                )
            }
        }

        options.forEachIndexed { index, optionText ->
            validateOptionText(optionText, index + 1)
        }

        val duplicateOptions = options.groupingBy { it.trim() }
            .eachCount()
            .filter { it.value > 1 }
            .keys

        if (duplicateOptions.isNotEmpty()) {
            throw QuestionOptionValidationException(
                "중복된 옵션이 존재합니다: ${duplicateOptions.joinToString(", ")}"
            )
        }
    }
    private fun validateOptionText(optionText: String, optionNumber: Int) {
        when {
            optionText.isBlank() -> {
                throw QuestionOptionValidationException(
                    "${optionNumber}번째 옵션의 텍스트는 비어있을 수 없습니다."
                )
            }
            optionText.length < MIN_OPTION_TEXT_LENGTH -> {
                throw QuestionOptionValidationException(
                    "${optionNumber}번째 옵션의 텍스트는 최소 ${MIN_OPTION_TEXT_LENGTH}자 이상이어야 합니다. (현재: ${optionText.length}자)"
                )
            }
            optionText.length > MAX_OPTION_TEXT_LENGTH -> {
                throw QuestionOptionValidationException(
                    "${optionNumber}번째 옵션의 텍스트는 최대 ${MAX_OPTION_TEXT_LENGTH}자까지 입력 가능합니다. (현재: ${optionText.length}자)"
                )
            }
        }
    }

    private fun validateRequiredQuestionRules(questionType: QuestionType, isRequired: Boolean) {

        when (questionType) {
            QuestionType.EMAIL -> {

            }
            else -> {

            }
        }
    }

    fun validateQuestionCountLimit(currentCount: Int) {
        if (currentCount >= MAX_QUESTIONS_PER_SURVEY) {
            throw QuestionValidationException(
                "설문당 최대 ${MAX_QUESTIONS_PER_SURVEY}개의 질문만 추가할 수 있습니다. (현재: ${currentCount}개)"
            )
        }
    }

    fun validateQuestionOrder(order: Int, currentQuestionCount: Int) {
        when {
            order < 1 -> {
                throw QuestionValidationException(
                    "질문 순서는 1 이상이어야 합니다. (현재: $order)"
                )
            }
            order > currentQuestionCount + 1 -> {
                throw QuestionValidationException(
                    "질문 순서는 현재 질문 개수 + 1을 초과할 수 없습니다. (현재: $order, 최대: ${currentQuestionCount + 1})"
                )
            }
        }
    }

    fun validateQuestionModification(
        existingQuestionType: QuestionType,
        newQuestionType: QuestionType,
        existingOptions: List<String>,
        newOptions: List<String>
    ) {

        if (existingQuestionType != newQuestionType) {
                false;
        }


        if (existingQuestionType.supportsOptions() && newQuestionType.supportsOptions()) {

            val removedOptions = existingOptions.toSet() - newOptions.toSet()
            if (removedOptions.isNotEmpty()) {
               true;
            }
        }
    }
}
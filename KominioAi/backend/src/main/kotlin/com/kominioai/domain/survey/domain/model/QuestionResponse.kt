package com.kominioai.domain.survey.domain.model

data class QuestionResponse(
    val questionId: QuestionId,
    val answer: Any?
) {
    fun validate(type: QuestionType, required: Boolean) {
        if (required && (answer == null || (answer is String && answer.isBlank()) || (answer is List<*> && answer.isEmpty()))) {
            throw IllegalArgumentException("필수 질문에 응답이 필요합니다.")
        }

        when (type) {
            QuestionType.MULTIPLE_CHOICE, QuestionType.QUIZ_MULTIPLE_CHOICE -> {
                if (answer !is String && answer !is List<*>) {
                    throw IllegalArgumentException("객관식 답변은 String 또는 String List여야 합니다.")
                }
                if (answer is String && answer.isBlank() && required) {
                    throw IllegalArgumentException("객관식 답변이 비어 있습니다.")
                }
                if (answer is List<*> && answer.isEmpty() && required) {
                    throw IllegalArgumentException("객관식(다중선택) 답변이 비어 있습니다.")
                }
            }
            QuestionType.ESSAY, QuestionType.QUIZ_ESSAY, QuestionType.SHORT_ANSWER, QuestionType.QUIZ_SHORT_ANSWER -> {
                if (answer !is String) {
                    throw IllegalArgumentException("주관식 답변은 String이어야 합니다.")
                }
                if (answer.isBlank() && required) {
                    throw IllegalArgumentException("주관식 답변이 비어 있습니다.")
                }
            }
        }
    }

    fun asString(): String? = answer as? String
    fun asStringList(): List<String>? = when (answer) {
        is List<*> -> (answer as? List<String>)
        is String -> listOf(answer as String)
        else -> null
    }
}
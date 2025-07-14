package com.kominioai.domain.survey.domain.model

data class Question private constructor(
    val id: QuestionId,
    val content: String,
    val type: QuestionType,
    val order: Int,
    val isRequired: Boolean,
    val options: List<QuestionOption>
) {
    companion object {
        fun create(
            content: String,
            type: QuestionType,
            order: Int,
            isRequired: Boolean,
            options: List<String> = emptyList()
        ): Question {
            require(content.isNotBlank()) { "질문 내용은 필수입니다." }
            require(content.length <= 500) { "질문 내용은 500자 이내여야 합니다." }
            require(order > 0) { "질문 순서는 1 이상이어야 합니다." }

            val questionOptions = when (type) {
                QuestionType.MULTIPLE_CHOICE, QuestionType.QUIZ_MULTIPLE_CHOICE -> {
                    require(options.size >= 2) { "선택형 질문은 최소 2개 이상의 옵션이 필요합니다." }
                    require(options.size <= 10) { "선택형 질문은 최대 10개까지의 옵션만 가능합니다." }
                    options.mapIndexed { index, option ->
                        QuestionOption.create(option, index + 1)
                    }
                }
                else -> {
                    require(options.isEmpty()) { "주관식 질문에는 옵션을 설정할 수 없습니다." }
                    emptyList()
                }
            }

            return Question(
                id = QuestionId.generate(),
                content = content,
                type = type,
                order = order,
                isRequired = isRequired,
                options = questionOptions
            )
        }
    }

    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        if (content.isBlank()) {
            errors.add("질문 내용은 필수입니다.")
        }

        if (content.length > 500) {
            errors.add("질문 내용은 500자 이내여야 합니다.")
        }

        when (type) {
            QuestionType.MULTIPLE_CHOICE, QuestionType.QUIZ_MULTIPLE_CHOICE -> {
                if (options.size < 2) {
                    errors.add("선택형 질문은 최소 2개 이상의 옵션이 필요합니다.")
                }
                if (options.size > 10) {
                    errors.add("선택형 질문은 최대 10개까지의 옵션만 가능합니다.")
                }
            }
            else -> {
                if (options.isNotEmpty()) {
                    errors.add("주관식 질문에는 옵션을 설정할 수 없습니다.")
                }
            }
        }

        return errors
    }

    fun isValid(): Boolean {
        return validate().isEmpty()
    }
}
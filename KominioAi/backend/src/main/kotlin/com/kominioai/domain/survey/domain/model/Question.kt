package com.kominioai.domain.survey.domain.model

import java.util.UUID

class Question private constructor(
    val id: QuestionId,
    private var content: String,
    val type: QuestionType,
    private var order: Int,
    private var isRequired: Boolean,
    private val options: MutableList<QuestionOption>
) {
    
    companion object {
        private const val MAX_CONTENT_LENGTH = 500
        private const val MIN_CONTENT_LENGTH = 1
        private const val MAX_OPTIONS = 10
        private const val MIN_OPTIONS = 2

        fun create(
            content: String,
            type: QuestionType,
            order: Int,
            isRequired: Boolean,
            options: List<String> = emptyList()
        ): Question {
            require(content.isNotBlank()) { "질문 내용은 필수입니다." }
            require(content.length <= MAX_CONTENT_LENGTH) { "질문 내용은 ${MAX_CONTENT_LENGTH}자 이내여야 합니다." }
            require(order > 0) { "질문 순서는 1 이상이어야 합니다." }

            val questionOptions = when (type) {
                QuestionType.MULTIPLE_CHOICE, QuestionType.QUIZ_MULTIPLE_CHOICE -> {
                    require(options.size >= MIN_OPTIONS) { "선택형 질문은 최소 ${MIN_OPTIONS}개 이상의 옵션이 필요합니다." }
                    require(options.size <= MAX_OPTIONS) { "선택형 질문은 최대 ${MAX_OPTIONS}개까지의 옵션만 가능합니다." }
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
                content = content.trim(),
                type = type,
                order = order,
                isRequired = isRequired,
                options = questionOptions.toMutableList()
            )
        }

        fun reconstruct(
            id: String,
            content: String,
            type: String,
            order: Int,
            isRequired: Boolean,
            options: List<QuestionOption>
        ): Question {
            return Question(
                id = QuestionId.fromString(id),
                content = content,
                type = QuestionType.valueOf(type),
                order = order,
                isRequired = isRequired,
                options = options.toMutableList()
            )
        }
    }

    fun updateContent(newContent: String): Question {
        require(newContent.isNotBlank()) { "질문 내용은 필수입니다." }
        require(newContent.length <= MAX_CONTENT_LENGTH) { "질문 내용은 ${MAX_CONTENT_LENGTH}자 이내여야 합니다." }
        
        content = newContent.trim()
        return this
    }

    fun updateOrder(newOrder: Int): Question {
        require(newOrder > 0) { "질문 순서는 1 이상이어야 합니다." }
        
        order = newOrder
        return this
    }

    fun setRequired(required: Boolean): Question {
        isRequired = required
        return this
    }

    fun addOption(optionContent: String): Question {
        require(type == QuestionType.MULTIPLE_CHOICE || type == QuestionType.QUIZ_MULTIPLE_CHOICE) {
            "선택형 질문에만 옵션을 추가할 수 있습니다."
        }
        require(options.size < MAX_OPTIONS) { "최대 ${MAX_OPTIONS}개까지의 옵션만 가능합니다." }
        require(optionContent.isNotBlank()) { "옵션 내용은 비어있을 수 없습니다." }

        val newOption = QuestionOption.create(optionContent, options.size + 1)
        options.add(newOption)
        return this
    }

    fun removeOption(optionId: QuestionOptionId): Question {
        require(type == QuestionType.MULTIPLE_CHOICE || type == QuestionType.QUIZ_MULTIPLE_CHOICE) {
            "선택형 질문에만 옵션을 삭제할 수 있습니다."
        }
        require(options.size > MIN_OPTIONS) { "최소 ${MIN_OPTIONS}개 이상의 옵션이 필요합니다." }

        val removed = options.removeIf { it.id == optionId }
        require(removed) { "존재하지 않는 옵션입니다." }

        options.forEachIndexed { index, option ->
            option.updateOrder(index + 1)
        }
        return this
    }

    fun updateOption(optionId: QuestionOptionId, newContent: String): Question {
        require(type == QuestionType.MULTIPLE_CHOICE || type == QuestionType.QUIZ_MULTIPLE_CHOICE) {
            "선택형 질문에만 옵션을 수정할 수 있습니다."
        }
        require(newContent.isNotBlank()) { "옵션 내용은 비어있을 수 없습니다." }

        val option = options.find { it.id == optionId }
        require(option != null) { "존재하지 않는 옵션입니다." }

        option.updateContent(newContent)
        return this
    }

    fun getContent(): String = content
    fun getOrder(): Int = order
    fun isRequired(): Boolean = isRequired
    fun getOptions(): List<QuestionOption> = options.toList()
    fun getOptionCount(): Int = options.size

    fun isMultipleChoice(): Boolean = 
        type == QuestionType.MULTIPLE_CHOICE || type == QuestionType.QUIZ_MULTIPLE_CHOICE

    fun isEssay(): Boolean = 
        type == QuestionType.ESSAY || type == QuestionType.QUIZ_ESSAY

    fun isShortAnswer(): Boolean = 
        type == QuestionType.SHORT_ANSWER || type == QuestionType.QUIZ_SHORT_ANSWER

    fun isQuiz(): Boolean = 
        type.name.startsWith("QUIZ_")

    fun validate(): List<String> {
        val errors = mutableListOf<String>()

        if (content.isBlank()) {
            errors.add("질문 내용은 필수입니다.")
        }

        if (content.length > MAX_CONTENT_LENGTH) {
            errors.add("질문 내용은 ${MAX_CONTENT_LENGTH}자 이내여야 합니다.")
        }

        when (type) {
            QuestionType.MULTIPLE_CHOICE, QuestionType.QUIZ_MULTIPLE_CHOICE -> {
                if (options.size < MIN_OPTIONS) {
                    errors.add("선택형 질문은 최소 ${MIN_OPTIONS}개 이상의 옵션이 필요합니다.")
                }
                if (options.size > MAX_OPTIONS) {
                    errors.add("선택형 질문은 최대 ${MAX_OPTIONS}개까지의 옵션만 가능합니다.")
                }
                options.forEach { option ->
                    if (option.getContent().isBlank()) {
                        errors.add("옵션 내용은 비어있을 수 없습니다.")
                    }
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

    fun isValid(): Boolean = validate().isEmpty()
}
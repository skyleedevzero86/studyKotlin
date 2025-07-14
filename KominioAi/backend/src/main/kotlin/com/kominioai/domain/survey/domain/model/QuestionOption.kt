package com.kominioai.domain.survey.domain.model

data class QuestionOption private constructor(
    val id: QuestionOptionId,
    private var content: String,
    private var order: Int
) {
    companion object {
        fun create(content: String, order: Int): QuestionOption {
            require(content.isNotBlank()) { "옵션 내용은 필수입니다." }
            require(content.length <= 200) { "옵션 내용은 200자 이내여야 합니다." }
            require(order > 0) { "옵션 순서는 1 이상이어야 합니다." }

            return QuestionOption(
                id = QuestionOptionId.generate(),
                content = content,
                order = order
            )
        }

        fun reconstruct(
            id: String,
            content: String,
            order: Int
        ): QuestionOption {
            return QuestionOption(
                id = QuestionOptionId.fromString(id),
                content = content,
                order = order
            )
        }
    }

    fun updateContent(newContent: String): QuestionOption {
        require(newContent.isNotBlank()) { "옵션 내용은 필수입니다." }
        require(newContent.length <= 200) { "옵션 내용은 200자 이내여야 합니다." }
        content = newContent
        return this
    }

    fun updateOrder(newOrder: Int): QuestionOption {
        require(newOrder > 0) { "옵션 순서는 1 이상이어야 합니다." }
        order = newOrder
        return this
    }

    fun getContent(): String = content
    fun getOrder(): Int = order
}
package com.kominioai.domain.survey.domain.model

data class Question(
    val id: Long? = null,
    val content: String,
    val type: QuestionType,
    val order: Int,
    val options: List<QuestionOption> = emptyList()
) {
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        if (content.isBlank()) errors.add("질문 내용은 필수입니다.")
        if (type.name.contains("MULTIPLE_CHOICE") && options.size < 2) errors.add("객관식은 최소 2개 이상의 선택지가 필요합니다.")
        return errors
    }
}
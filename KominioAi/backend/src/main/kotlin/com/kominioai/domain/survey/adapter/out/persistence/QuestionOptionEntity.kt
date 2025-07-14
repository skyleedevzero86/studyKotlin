package com.kominioai.domain.survey.adapter.out.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("question_options")
data class QuestionOptionEntity(
    @Id
    val id: String? = null,
    val questionId: String,
    val content: String,
    val order: Int
) {
    companion object {
        fun fromDomain(option: com.kominioai.domain.survey.domain.model.QuestionOption, questionId: String): QuestionOptionEntity =
            QuestionOptionEntity(
                id = option.id.value,
                questionId = questionId,
                content = option.getContent(),
                order = option.getOrder()
            )
    }

    fun toDomain(): com.kominioai.domain.survey.domain.model.QuestionOption =
        com.kominioai.domain.survey.domain.model.QuestionOption.reconstruct(
            id = id ?: "",
            content = content,
            order = order
        )
} 
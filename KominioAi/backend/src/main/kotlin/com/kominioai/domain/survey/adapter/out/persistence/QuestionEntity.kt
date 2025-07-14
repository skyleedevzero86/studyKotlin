package com.kominioai.domain.survey.adapter.out.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("questions")
data class QuestionEntity(
    @Id
    val id: String? = null,
    val surveyId: String,
    val content: String,
    val type: String,
    val order: Int,
    val isRequired: Boolean
) {
    companion object {
        fun fromDomain(question: com.kominioai.domain.survey.domain.model.Question, surveyId: String): QuestionEntity =
            QuestionEntity(
                id = question.id.value,
                surveyId = surveyId,
                content = question.getContent(),
                type = question.type.name,
                order = question.getOrder(),
                isRequired = question.isRequired()
            )
    }

    fun toDomain(): com.kominioai.domain.survey.domain.model.Question =
        com.kominioai.domain.survey.domain.model.Question.reconstruct(
            id = id ?: "",
            content = content,
            type = type,
            order = order,
            isRequired = isRequired,
            options = emptyList()
        )
}
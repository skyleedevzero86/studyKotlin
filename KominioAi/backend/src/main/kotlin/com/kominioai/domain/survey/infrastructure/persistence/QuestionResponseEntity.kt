package com.kominioai.domain.survey.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("question_responses")
data class QuestionResponseEntity(
    @Id
    val id: String? = null,
    val participationId: String,
    val questionId: String,
    val answer: String?,
    val answerType: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun fromDomain(
            response: com.kominioai.domain.survey.domain.model.QuestionResponse,
            participationId: String
        ): QuestionResponseEntity {
            val (answerString, answerType) = when (response.answer) {
                is String -> response.answer to "STRING"
                is List<*> -> {
                    val jsonList = (response.answer as List<*>).map { it.toString() }
                    com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(jsonList) to "LIST"
                }
                is Boolean -> response.answer.toString() to "BOOLEAN"
                is Number -> response.answer.toString() to "NUMBER"
                null -> null to "NULL"
                else -> response.answer.toString() to "STRING"
            }

            return QuestionResponseEntity(
                id = null,
                participationId = participationId,
                questionId = response.questionId.value,
                answer = answerString,
                answerType = answerType,
                createdAt = LocalDateTime.now()
            )
        }
    }

    fun toDomain(): com.kominioai.domain.survey.domain.model.QuestionResponse {
        val answer: Any? = when (answerType) {
            "STRING" -> answer
            "LIST" -> {
                if (answer != null) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper()
                            .readValue(answer, Array<String>::class.java)
                            .toList()
                    } catch (e: Exception) {
                        answer
                    }
                } else null
            }
            "BOOLEAN" -> answer?.toBoolean()
            "NUMBER" -> answer?.toLongOrNull() ?: answer?.toDoubleOrNull() ?: answer
            "NULL" -> null
            else -> answer
        }

        return com.kominioai.domain.survey.domain.model.QuestionResponse(
            questionId = com.kominioai.domain.survey.domain.model.QuestionId.fromString(questionId),
            answer = answer
        )
    }
} 
package com.kominioai.domain.survey.domain.model.domain

import com.kominioai.domain.survey.domain.model.SurveySettings
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import java.time.LocalDateTime

data class Survey(
    val id: SurveyId,
    val title: String,
    val description: String?,
    val createdBy: UserId,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val status: SurveyStatus,
    val questions: List<Question>,
    val settings: SurveySettings
) {
    fun addQuestion(question: Question): Survey {
        return copy(
            questions = questions + question,
            updatedAt = LocalDateTime.now()
        )
    }

    fun publish(): Survey {
        return copy(
            status = SurveyStatus.PUBLISHED,
            updatedAt = LocalDateTime.now()
        )
    }

    companion object {
        fun create(
            title: String,
            description: String?,
            createdBy: UserId,
            settings: SurveySettings
        ): Survey {
            val now = LocalDateTime.now()
            return Survey(
                id = SurveyId.generate(),
                title = title,
                description = description,
                createdBy = createdBy,
                createdAt = now,
                updatedAt = now,
                status = SurveyStatus.DRAFT,
                questions = emptyList(),
                settings = settings
            )
        }
    }
}
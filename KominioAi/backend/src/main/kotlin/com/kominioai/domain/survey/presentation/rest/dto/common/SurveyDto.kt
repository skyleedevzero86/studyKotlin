package com.kominioai.domain.survey.presentation.rest.dto.common

import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.model.SurveySettings
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import java.time.LocalDateTime

data class SurveyDto(
    val id: String,
    val title: String,
    val description: String?,
    val createdBy: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val status: SurveyStatus,
    val questions: List<QuestionDto>,
    val settings: SurveySettings
) {
    companion object {
        fun from(survey: Survey): SurveyDto {
            return SurveyDto(
                id = survey.id.value,
                title = survey.title,
                description = survey.description,
                createdBy = survey.createdBy.value,
                createdAt = survey.createdAt,
                updatedAt = survey.updatedAt,
                status = survey.status,
                questions = survey.questions.map { QuestionDto.from(it) },
                settings = survey.settings
            )
        }
    }
}
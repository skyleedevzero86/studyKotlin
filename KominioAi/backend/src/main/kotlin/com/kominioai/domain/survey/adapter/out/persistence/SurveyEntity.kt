package com.kominioai.domain.survey.adapter.out.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("surveys")
data class SurveyEntity(
    @Id
    val id: String? = null,
    val title: String,
    val author: String,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val participantCount: Int,
    val targetType: String,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val duration: String,
    val surveyType: String,
    val participantType: String
) {
    fun toDomain(): com.kominioai.domain.survey.domain.model.Survey =
        com.kominioai.domain.survey.domain.model.Survey.reconstruct(
            id = id ?: "",
            title = title,
            author = author,
            status = status,
            startDate = startDate ?: LocalDateTime.now(),
            endDate = endDate ?: LocalDateTime.now(),
            participantCount = participantCount,
            targetType = targetType,
            surveyType = surveyType,
            participantType = participantType,
            timeLimit = null,
            questions = emptyList(),
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    companion object {
        fun fromDomain(survey: com.kominioai.domain.survey.domain.model.Survey): SurveyEntity =
            SurveyEntity(
                id = survey.id.value,
                title = survey.title.value,
                author = survey.author.name,
                status = survey.status.name,
                createdAt = survey.createdAt,
                updatedAt = survey.updatedAt,
                participantCount = survey.participantCount,
                targetType = survey.targetType.name,
                startDate = survey.period.startDate,
                endDate = survey.period.endDate,
                duration = survey.period.display(),
                surveyType = survey.surveyType.name,
                participantType = survey.participantType.name
            )
    }
}
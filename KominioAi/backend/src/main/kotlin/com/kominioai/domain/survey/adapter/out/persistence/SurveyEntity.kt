package com.kominioai.domain.survey.adapter.out.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("surveys")
data class SurveyEntity(
    @Id
    val id: Long? = null,
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
        com.kominioai.domain.survey.domain.model.Survey(
            id = id,
            title = title,
            author = com.kominioai.domain.survey.domain.model.Author(author),
            status = com.kominioai.domain.survey.domain.model.SurveyStatus.valueOf(status),
            createdAt = createdAt,
            updatedAt = updatedAt,
            participantCount = participantCount,
            targetType = com.kominioai.domain.survey.domain.model.TargetType.valueOf(targetType),
            startDate = startDate,
            endDate = endDate,
            duration = duration,
            surveyType = com.kominioai.domain.survey.domain.model.SurveyType.valueOf(surveyType),
            participantType = com.kominioai.domain.survey.domain.model.ParticipantType.valueOf(participantType),
            timeLimit = null,
            period = com.kominioai.domain.survey.domain.model.SurveyPeriod(
                startDate ?: LocalDateTime.now(),
                endDate ?: LocalDateTime.now()
            ),
            questions = emptyList()
        )

    companion object {
        fun fromDomain(survey: com.kominioai.domain.survey.domain.model.Survey): SurveyEntity =
            SurveyEntity(
                id = survey.id,
                title = survey.title,
                author = survey.author.name,
                status = survey.status.name,
                createdAt = survey.createdAt,
                updatedAt = survey.updatedAt,
                participantCount = survey.participantCount,
                targetType = survey.targetType.name,
                startDate = survey.startDate,
                endDate = survey.endDate,
                duration = survey.duration,
                surveyType = survey.surveyType.name,
                participantType = survey.participantType.name
            )
    }
}
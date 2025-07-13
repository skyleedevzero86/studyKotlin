package com.kominioai.domain.survey.adapter.out.cache

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import java.time.LocalDateTime

@RedisHash("survey")
data class SurveyCacheEntity(
    @Id
    val id: String? = null,

    @Indexed
    @JsonProperty("title")
    val title: String,

    @JsonProperty("author")
    val author: String,

    @Indexed
    @JsonProperty("status")
    val status: String,

    @JsonProperty("createdAt")
    val createdAt: LocalDateTime,

    @JsonProperty("updatedAt")
    val updatedAt: LocalDateTime,

    @JsonProperty("participantCount")
    val participantCount: Int,

    @JsonProperty("targetType")
    val targetType: String,

    @JsonProperty("startDate")
    val startDate: LocalDateTime?,

    @JsonProperty("endDate")
    val endDate: LocalDateTime?,

    @JsonProperty("duration")
    val duration: String,

    @JsonProperty("surveyType")
    val surveyType: String,

    @JsonProperty("participantType")
    val participantType: String,

    @JsonProperty("period")
    val period: String
) {
    fun toDomain(): com.kominioai.domain.survey.domain.model.Survey =
        com.kominioai.domain.survey.domain.model.Survey(
            id = id?.toLong(),
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
        fun fromDomain(survey: com.kominioai.domain.survey.domain.model.Survey): SurveyCacheEntity =
            SurveyCacheEntity(
                id = survey.id?.toString(),
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
                participantType = survey.participantType.name,
                period = survey.period.display()
            )
    }
}
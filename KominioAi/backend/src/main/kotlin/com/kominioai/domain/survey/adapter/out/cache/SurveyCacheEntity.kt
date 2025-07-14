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
    companion object {
        fun fromDomain(survey: com.kominioai.domain.survey.domain.model.Survey): SurveyCacheEntity =
            SurveyCacheEntity(
                id = survey.id.value,
                title = survey.getTitle().value,
                author = survey.author.name,
                status = survey.getStatus().name,
                createdAt = survey.createdAt,
                updatedAt = survey.getUpdatedAt(),
                participantCount = survey.getParticipationCount(),
                targetType = survey.targetType.name,
                startDate = survey.getPeriodStartDate(),
                endDate = survey.getPeriodEndDate(),
                duration = "${survey.getPeriodStartDate().toLocalDate()} ~ ${survey.getPeriodEndDate().toLocalDate()}",
                surveyType = survey.surveyType.name,
                participantType = survey.participantType.name,
                period = "${survey.getPeriodStartDate().toLocalDate()} ~ ${survey.getPeriodEndDate().toLocalDate()}"
            )
    }
}
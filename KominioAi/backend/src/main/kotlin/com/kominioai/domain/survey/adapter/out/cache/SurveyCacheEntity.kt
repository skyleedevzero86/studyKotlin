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
                participantType = survey.participantType.name,
                period = survey.period.display()
            )
    }
}
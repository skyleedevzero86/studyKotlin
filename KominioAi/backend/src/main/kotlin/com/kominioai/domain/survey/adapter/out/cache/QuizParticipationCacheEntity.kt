package com.kominioai.domain.survey.adapter.out.cache

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import java.time.LocalDateTime

@RedisHash("quiz-participation")
data class QuizParticipationCacheEntity(
    @Id
    val id: String? = null,

    @Indexed
    @JsonProperty("surveyId")
    val surveyId: String,

    @JsonProperty("participantName")
    val participantName: String,

    @JsonProperty("participantPhone")
    val participantPhone: String,

    @JsonProperty("userId")
    val userId: String?,

    @JsonProperty("startedAt")
    val startedAt: LocalDateTime,

    @JsonProperty("submittedAt")
    val submittedAt: LocalDateTime?,

    @Indexed
    @JsonProperty("status")
    val status: String
) {
    companion object {
        fun fromDomain(participation: com.kominioai.domain.survey.domain.model.QuizParticipation): QuizParticipationCacheEntity =
            QuizParticipationCacheEntity(
                id = participation.id.value,
                surveyId = participation.surveyId.value,
                participantName = participation.participant.name ?: "",
                participantPhone = participation.participant.phone ?: "",
                userId = participation.participant.userId,
                startedAt = participation.startedAt,
                submittedAt = participation.getSubmittedAt(),
                status = participation.getStatus().name
            )
    }

    fun toDomain(): com.kominioai.domain.survey.domain.model.QuizParticipation? {
        return try {
            com.kominioai.domain.survey.domain.model.QuizParticipation.reconstruct(
                id = id ?: return null,
                surveyId = surveyId,
                participant = com.kominioai.domain.survey.domain.model.ParticipantInfo(
                    userId = userId,
                    name = participantName,
                    phone = participantPhone,
                    authenticated = userId != null
                ),
                answers = emptyList(),
                startedAt = startedAt,
                submittedAt = submittedAt,
                status = com.kominioai.domain.survey.domain.model.ParticipationStatus.valueOf(status)
            )
        } catch (e: Exception) {
            null
        }
    }
}
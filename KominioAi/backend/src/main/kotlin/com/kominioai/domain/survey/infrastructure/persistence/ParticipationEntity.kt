package com.kominioai.domain.survey.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("survey_participations")
data class ParticipationEntity(
    @Id
    val id: String? = null,
    val surveyId: String,
    val userId: String?,
    val participantName: String?,
    val participantPhone: String?,
    val authenticated: Boolean,
    val status: String,
    val participatedAt: LocalDateTime,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromDomain(participation: com.kominioai.domain.survey.domain.model.SurveyParticipation): ParticipationEntity =
            ParticipationEntity(
                id = participation.id.value,
                surveyId = participation.surveyId.value,
                userId = participation.participant.userId,
                participantName = participation.participant.name,
                participantPhone = participation.participant.phone,
                authenticated = participation.participant.authenticated,
                status = participation.status.name,
                participatedAt = participation.participatedAt,
                createdAt = participation.participatedAt,
                updatedAt = participation.participatedAt
            )
    }

    fun toDomain(): com.kominioai.domain.survey.domain.model.SurveyParticipation =
        com.kominioai.domain.survey.domain.model.SurveyParticipation.reconstruct(
            id = id ?: "",
            surveyId = surveyId,
            participant = com.kominioai.domain.survey.domain.model.ParticipantInfo(
                userId = userId,
                name = participantName,
                phone = participantPhone,
                authenticated = authenticated
            ),
            responses = emptyList(),
            status = status,
            participatedAt = participatedAt
        )
}
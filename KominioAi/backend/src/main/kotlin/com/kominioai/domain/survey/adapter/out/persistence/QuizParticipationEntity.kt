package com.kominioai.domain.survey.adapter.out.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("quiz_participations")
data class QuizParticipationEntity(
    @Id
    val id: String? = null,
    val surveyId: String,
    val participantName: String,
    val participantPhone: String,
    val userId: String? = null,
    val startedAt: LocalDateTime,
    val submittedAt: LocalDateTime? = null,
    val status: String
) {
    companion object {
        fun fromDomain(participation: com.kominioai.domain.survey.domain.model.QuizParticipation): QuizParticipationEntity =
            QuizParticipationEntity(
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

    fun toDomain(): com.kominioai.domain.survey.domain.model.QuizParticipation =
        com.kominioai.domain.survey.domain.model.QuizParticipation.reconstruct(
            id = id ?: "",
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
}
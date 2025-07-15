package com.kominioai.domain.survey.domain.event

import com.kominioai.domain.survey.domain.model.ParticipationId
import com.kominioai.domain.survey.domain.model.SurveyId
import java.time.LocalDateTime

sealed class QuizParticipationEvent : DomainEvent() {
    data class ParticipationStarted(
        val participationId: ParticipationId,
        val surveyId: SurveyId,
        val participantName: String,
        val participantPhone: String,
        val startedAt: LocalDateTime
    ) : QuizParticipationEvent()

    data class AnswerSubmitted(
        val participationId: ParticipationId,
        val surveyId: SurveyId,
        val questionId: String,
        val submittedAt: LocalDateTime
    ) : QuizParticipationEvent()

    data class ParticipationSubmitted(
        val participationId: ParticipationId,
        val surveyId: SurveyId,
        val participantName: String,
        val submittedAt: LocalDateTime
    ) : QuizParticipationEvent()

    data class ParticipationTimeExpired(
        val participationId: ParticipationId,
        val surveyId: SurveyId,
        val expiredAt: LocalDateTime
    ) : QuizParticipationEvent()
}
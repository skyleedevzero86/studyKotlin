package com.kominioai.domain.survey.domain.event

import java.time.LocalDateTime

data class QuizParticipatedEvent(
    val surveyId: String,
    val participantId: String,
    val participatedAt: LocalDateTime = LocalDateTime.now()
) : DomainEvent()
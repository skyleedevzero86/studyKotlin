package com.kominioai.domain.survey.domain.event

import java.time.LocalDateTime

data class SurveyUpdatedEvent(
    val surveyId: String,
    val title: String,
    val updatedAt: LocalDateTime
) : DomainEvent()
package com.kominioai.domain.survey.domain.event

import java.time.LocalDateTime

data class SurveyClosedEvent(
    val surveyId: String,
    val closedAt: LocalDateTime
) : DomainEvent()
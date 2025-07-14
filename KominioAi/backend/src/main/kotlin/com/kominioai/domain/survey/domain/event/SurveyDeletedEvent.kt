package com.kominioai.domain.survey.domain.event

import java.time.LocalDateTime

data class SurveyDeletedEvent(
    val surveyId: String,
    val deletedAt: LocalDateTime
) : DomainEvent()
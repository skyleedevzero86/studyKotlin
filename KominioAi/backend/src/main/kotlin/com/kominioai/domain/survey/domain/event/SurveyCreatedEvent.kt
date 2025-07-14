package com.kominioai.domain.survey.domain.event

import java.time.LocalDateTime

data class SurveyCreatedEvent(
    val surveyId: String,
    val title: String,
    val author: String,
    val createdAt: LocalDateTime
) : DomainEvent()
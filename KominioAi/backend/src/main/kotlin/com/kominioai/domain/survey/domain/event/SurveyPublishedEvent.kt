package com.kominioai.domain.survey.domain.event

import java.time.LocalDateTime

data class SurveyPublishedEvent(
    val surveyId: String,
    val publishedAt: LocalDateTime
) : DomainEvent()
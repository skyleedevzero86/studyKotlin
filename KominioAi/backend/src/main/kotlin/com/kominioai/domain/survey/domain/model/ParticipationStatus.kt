package com.kominioai.domain.survey.domain.model

import java.time.LocalDateTime

data class ParticipationStatus(
    val currentCount: Int,
    val targetCount: Int?,
    val participationRate: Double,
    val lastUpdated: LocalDateTime
)
package com.kominioai.domain.survey.domain.model

import java.time.LocalDateTime

data class ParticipationStatistics(
    val currentCount: Int,
    val targetCount: Int?,
    val participationRate: Double,
    val lastUpdated: LocalDateTime
) 
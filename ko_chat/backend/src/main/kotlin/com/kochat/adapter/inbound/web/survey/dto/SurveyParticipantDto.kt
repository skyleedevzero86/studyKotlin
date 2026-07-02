package com.kochat.adapter.inbound.web.survey.dto

import com.kochat.domain.survey.model.ParticipantStatus
import java.time.LocalDateTime

data class SurveyParticipantDto(
    val userId: Long,
    val username: String,
    val displayName: String?,
    val status: ParticipantStatus,
    val assignedAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
)

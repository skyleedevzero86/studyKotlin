package com.kochat.adapter.inbound.web.survey.dto

import com.kochat.domain.survey.model.ParticipantStatus

data class SurveyParticipantStatisticsDto(
    val userId: Long,
    val username: String,
    val displayName: String?,
    val status: ParticipantStatus,
    val answers: List<ParticipantAnswerDto>,
)

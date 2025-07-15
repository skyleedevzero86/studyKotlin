package com.kominioai.domain.survey.application.dto

import java.time.LocalDateTime

data class QuizParticipationResponse(
    val participationId: String,
    val surveyId: String,
    val participantName: String,
    val status: String,
    val startedAt: LocalDateTime,
    val submittedAt: LocalDateTime? = null,
    val timeLimit: Int? = null,
    val remainingTime: Long = -1L
)
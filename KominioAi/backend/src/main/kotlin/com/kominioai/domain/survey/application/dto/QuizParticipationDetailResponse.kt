package com.kominioai.domain.survey.application.dto

import java.time.LocalDateTime

data class QuizParticipationDetailResponse(
    val participationId: String,
    val surveyId: String,
    val surveyTitle: String,
    val participantName: String,
    val participantPhone: String,
    val status: String,
    val startedAt: LocalDateTime,
    val submittedAt: LocalDateTime?,
    val timeLimit: Int?,
    val remainingTime: Long,
    val answers: List<QuizAnswerResponse>
)
package com.kominioai.domain.survey.adapter.`in`.web.dto

import java.time.LocalDateTime


data class QuizParticipantRow(
    val participationId: String,
    val participantName: String,
    val participantPhone: String,
    val status: String,
    val startedAt: LocalDateTime,
    val submittedAt: LocalDateTime?,
    val answerCount: Int
)
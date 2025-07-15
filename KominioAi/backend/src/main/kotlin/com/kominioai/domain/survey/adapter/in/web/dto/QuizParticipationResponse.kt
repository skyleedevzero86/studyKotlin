package com.kominioai.domain.survey.adapter.`in`.web.dto

import com.kominioai.domain.survey.application.dto.QuizParticipationResponse as ApplicationResponse
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
) {
    companion object {
        fun from(response: ApplicationResponse) = QuizParticipationResponse(
            participationId = response.participationId,
            surveyId = response.surveyId,
            participantName = response.participantName,
            status = response.status,
            startedAt = response.startedAt,
            submittedAt = response.submittedAt,
            timeLimit = response.timeLimit,
            remainingTime = response.remainingTime
        )
    }
} 
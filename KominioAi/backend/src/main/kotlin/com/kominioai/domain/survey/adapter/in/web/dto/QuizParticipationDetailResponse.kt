package com.kominioai.domain.survey.adapter.`in`.web.dto

import com.kominioai.domain.survey.application.dto.QuizParticipationDetailResponse as ApplicationResponse
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
) {
    companion object {
        fun from(response: ApplicationResponse) = QuizParticipationDetailResponse(
            participationId = response.participationId,
            surveyId = response.surveyId,
            surveyTitle = response.surveyTitle,
            participantName = response.participantName,
            participantPhone = response.participantPhone,
            status = response.status,
            startedAt = response.startedAt,
            submittedAt = response.submittedAt,
            timeLimit = response.timeLimit,
            remainingTime = response.remainingTime,
            answers = response.answers.map { QuizAnswerResponse.from(it) }
        )
    }
}
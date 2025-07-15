package com.kominioai.domain.survey.adapter.`in`.web.dto

import java.time.LocalDateTime

data class QuizParticipantListResponse(
    val total: Long,
    val participants: List<QuizParticipantRow>
) {
    companion object {
        fun empty() = QuizParticipantListResponse(0, emptyList())
    }
}

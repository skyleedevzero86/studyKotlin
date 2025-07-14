package com.kominioai.domain.survey.application.dto.result

import java.time.LocalDateTime

data class SurveyResultDto(
    val surveyId: String,
    val totalParticipants: Int,
    val questions: List<QuestionResultDto>,
    val calculatedAt: LocalDateTime
)
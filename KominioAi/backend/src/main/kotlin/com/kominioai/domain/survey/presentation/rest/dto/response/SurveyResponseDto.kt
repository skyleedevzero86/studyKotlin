package com.kominioai.domain.survey.presentation.rest.dto.response

import java.time.Instant

data class SurveyResponseDto(
    val id: String,
    val surveyId: String,
    val respondentId: String?,
    val submittedAt: Instant,
    val answers: List<AnswerDto> = emptyList()
)
package com.kominioai.domain.survey.presentation.rest.dto.response

import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import java.time.Instant

data class SurveyDto(
    val id: String,
    val title: String,
    val description: String?,
    val status: SurveyStatus,
    val createdBy: String,
    val createdAt: Instant,
    val publishedAt: Instant?,
    val closedAt: Instant?,
    val questions: List<QuestionDto> = emptyList()
)
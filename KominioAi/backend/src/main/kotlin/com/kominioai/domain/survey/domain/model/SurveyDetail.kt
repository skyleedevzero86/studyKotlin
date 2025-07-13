package com.kominioai.domain.survey.domain.model

import java.time.LocalDateTime

data class SurveyDetail(
    val survey: Survey,
    val questions: List<Question>,
    val participantCount: Int,
    val viewCount: Int,
    val requirementLevel: RequirementLevel,
    val status: SurveyStatus,
    val theme: SurveyTheme,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
package com.kominioai.domain.survey.application.dto

import com.kominioai.domain.survey.domain.model.*

data class SurveyDetailResponse(
    val id: Long,
    val title: String,
    val author: String,
    val status: String,
    val type: String,
    val createdAt: String,
    val updatedAt: String,
    val displayInfo: SurveyDisplayInfo,
    val questions: List<QuestionPreviewDto>,
    val totalQuestionCount: Int,
    val hasMoreQuestions: Boolean,
    val navigation: NavigationInfoDto
)
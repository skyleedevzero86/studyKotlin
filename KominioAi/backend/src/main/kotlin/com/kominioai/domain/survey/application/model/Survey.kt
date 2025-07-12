package com.kominioai.domain.survey.application.model

import java.time.LocalDateTime

data class Survey(
    val id: Long,
    val title: String,
    val author: Author,
    val status: SurveyStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val participantCount: Int,
    val targetType: TargetType,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val duration: String
) {
    fun canDelete(): Boolean = status != SurveyStatus.IN_PROGRESS
    fun canEdit(): Boolean = status == SurveyStatus.PENDING
}
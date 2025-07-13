package com.kominioai.domain.survey.adapter.`in`.web.dto

import com.kominioai.domain.survey.domain.model.Survey
import java.time.LocalDateTime

data class SurveyRow(
    val id: Long?,
    val title: String,
    val author: String,
    val participantCount: Int,
    val targetType: String,
    val status: String,
    val createdAt: String,
    val startDate: String?,
    val endDate: String?,
    val duration: String
) {
    companion object {
        fun from(s: Survey) = SurveyRow(
            id = s.id,
            title = s.title,
            author = s.author.name,
            participantCount = s.participantCount,
            targetType = s.targetType.name,
            status = s.status.name,
            createdAt = s.createdAt.toString(),
            startDate = s.startDate?.toString(),
            endDate = s.endDate?.toString(),
            duration = s.duration
        )
    }
}
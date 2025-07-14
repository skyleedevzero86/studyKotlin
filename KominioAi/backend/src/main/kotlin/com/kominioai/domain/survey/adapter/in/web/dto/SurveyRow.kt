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
            id = s.id.value.toLongOrNull(),
            title = s.title.value,
            author = s.author.name,
            participantCount = s.participantCount,
            targetType = s.targetType.name,
            status = s.status.name,
            createdAt = s.createdAt.toString(),
            startDate = s.period.startDate.toString(),
            endDate = s.period.endDate.toString(),
            duration = s.period.display()
        )
    }
}
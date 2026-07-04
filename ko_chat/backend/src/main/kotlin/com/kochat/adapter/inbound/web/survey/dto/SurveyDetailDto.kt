package com.kochat.adapter.inbound.web.survey.dto

import com.kochat.domain.survey.model.SurveyStatus
import com.kochat.domain.survey.model.TargetMode
import java.time.LocalDateTime

data class SurveyDetailDto(
    val id: Long,
    val chatRoomId: Long?,
    val chatRoomName: String,
    val title: String,
    val description: String?,
    val status: SurveyStatus,
    val targetMode: TargetMode,
    val randomTargetCount: Int?,
    val startAt: LocalDateTime?,
    val endAt: LocalDateTime?,
    val questions: List<SurveyQuestionDto>,
    val participants: List<SurveyParticipantDto>,
    val createdByUserId: Long,
    val createdAt: LocalDateTime?,
    val canRespond: Boolean = false,
    val hasResponded: Boolean = false,
)

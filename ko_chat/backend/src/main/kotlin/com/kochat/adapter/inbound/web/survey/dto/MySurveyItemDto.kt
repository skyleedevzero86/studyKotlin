package com.kochat.adapter.inbound.web.survey.dto

import java.time.LocalDateTime

data class MySurveyItemDto(
    val surveyId: Long,
    val title: String,
    val description: String?,
    val status: String,
    val chatRoomId: Long?,
    val chatRoomName: String?,
    val startAt: LocalDateTime?,
    val endAt: LocalDateTime?,
    val hasResponded: Boolean,
    val canRespond: Boolean,
    val waitingForStart: Boolean,
)

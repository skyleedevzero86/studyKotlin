package com.kochat.adapter.inbound.web.survey.dto

data class SurveyRoomStatisticsDto(
    val chatRoomId: Long,
    val chatRoomName: String,
    val surveyCount: Long,
    val respondentCount: Long,
    val completedCount: Long,
)

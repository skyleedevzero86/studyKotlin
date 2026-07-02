package com.kochat.adapter.inbound.web.survey.dto

data class SurveyStatisticsDto(
    val surveyId: Long,
    val title: String,
    val totalParticipants: Long,
    val completedParticipants: Long,
    val byQuestion: List<SurveyQuestionStatisticsDto>,
    val byParticipant: List<SurveyParticipantStatisticsDto>,
)

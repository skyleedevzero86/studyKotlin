package com.kochat.adapter.inbound.web.survey.dto

data class ParticipantAnswerDto(
    val questionId: Long,
    val questionText: String,
    val optionTexts: List<String>,
    val textAnswer: String?,
)

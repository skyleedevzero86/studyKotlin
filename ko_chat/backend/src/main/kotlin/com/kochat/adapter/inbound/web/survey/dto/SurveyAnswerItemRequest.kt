package com.kochat.adapter.inbound.web.survey.dto

data class SurveyAnswerItemRequest(
    val questionId: Long,
    val optionIds: List<Long> = emptyList(),
    val textAnswer: String? = null,
)

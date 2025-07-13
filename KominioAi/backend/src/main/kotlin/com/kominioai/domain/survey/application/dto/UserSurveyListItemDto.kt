package com.kominioai.domain.survey.application.dto


data class UserSurveyListItemDto(
    val number: Long,
    val id: Long,
    val title: String,
    val author: String,
    val status: String,
    val surveyType: String,
    val period: String,
    val createdAt: String
)
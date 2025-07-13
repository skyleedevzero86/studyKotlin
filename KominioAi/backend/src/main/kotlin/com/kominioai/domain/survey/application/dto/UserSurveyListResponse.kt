package com.kominioai.domain.survey.application.dto

data class UserSurveyListResponse(
    val totalCount: Long,
    val surveys: List<UserSurveyListItemDto>
)
package com.kominioai.domain.survey.application.dto

data class NavigationInfoDto(
    val prevSurveyId: Long?,
    val nextSurveyId: Long?,
    val breadcrumb: List<String>
)
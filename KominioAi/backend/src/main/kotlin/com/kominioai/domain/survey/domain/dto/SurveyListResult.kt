package com.kominioai.domain.survey.domain.dto

import com.kominioai.domain.survey.application.model.Survey


data class SurveyListResult(
    val total: Long,
    val surveys: List<Survey>
)
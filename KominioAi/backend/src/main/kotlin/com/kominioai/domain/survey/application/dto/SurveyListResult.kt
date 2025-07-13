package com.kominioai.domain.survey.application.dto

import com.kominioai.domain.survey.domain.model.Survey


data class SurveyListResult(
    val total: Long,
    val surveys: List<Survey>
)
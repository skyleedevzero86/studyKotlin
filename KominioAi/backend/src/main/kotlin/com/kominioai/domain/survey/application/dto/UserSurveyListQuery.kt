package com.kominioai.domain.survey.application.dto

import com.kominioai.domain.survey.domain.model.SurveyStatus
import com.kominioai.domain.survey.domain.model.SurveyType
import java.time.LocalDate

data class UserSurveyListQuery(
    val title: String? = null,
    val status: SurveyStatus? = null,
    val surveyType: SurveyType? = null,
    val start: LocalDate? = null,
    val end: LocalDate? = null,
    val page: Int = 1,
    val size: Int = 10
)
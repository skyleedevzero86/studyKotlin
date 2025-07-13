package com.kominioai.domain.survey.domain.model

import java.time.LocalDateTime

data class SurveyPeriod(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
) {
    fun display(): String = "${startDate.toLocalDate()} ~ ${endDate.toLocalDate()}"
}
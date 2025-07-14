package com.kominioai.domain.survey.domain.model

import java.time.LocalDateTime

data class SurveyPeriod(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
) {
    init {
        require(!startDate.isAfter(endDate)) { "시작일은 종료일보다 이전이어야 합니다." }
    }

    fun display(): String = "${startDate.toLocalDate()} ~ ${endDate.toLocalDate()}"
}
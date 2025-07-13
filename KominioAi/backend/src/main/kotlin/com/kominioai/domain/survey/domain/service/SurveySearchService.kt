package com.kominioai.domain.survey.domain.service

import com.kominioai.domain.survey.domain.model.SurveyStatus
import com.kominioai.domain.survey.domain.model.SurveyType
import java.time.LocalDate

object SurveySearchService {
    fun validateSearch(
        title: String?,
        status: SurveyStatus?,
        type: SurveyType?,
        start: LocalDate?,
        end: LocalDate?
    ) {
        if (title != null && title.length > 100) throw IllegalArgumentException("설문명은 100자 이내여야 합니다.")
        if (start != null && end != null && start.isAfter(end)) throw IllegalArgumentException("시작일은 종료일보다 이전이어야 합니다.")
    }
}
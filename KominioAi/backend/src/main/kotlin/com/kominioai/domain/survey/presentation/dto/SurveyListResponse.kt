package com.kominioai.domain.survey.presentation.dto

import SurveyRow
import com.kominioai.domain.survey.domain.dto.SurveyListResult

data class SurveyListResponse(
    val total: Long,
    val surveys: List<SurveyRow>
) {
    companion object {
        fun from(result: SurveyListResult) = SurveyListResponse(
            total = result.total,
            surveys = result.surveys.map { SurveyRow.from(it) }
        )
    }
}
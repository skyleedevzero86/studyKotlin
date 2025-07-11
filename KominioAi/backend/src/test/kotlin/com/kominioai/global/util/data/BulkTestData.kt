package com.kominioai.global.util.data

import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.model.SurveyResponse

data class BulkTestData(
    val surveys: List<Survey>,
    val responses: List<SurveyResponse>
)
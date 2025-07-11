package com.kominioai.global.util.data

import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.Survey
import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.SurveyResponse

data class BulkTestData(
    val surveys: List<Survey>,
    val responses: List<SurveyResponse>
)
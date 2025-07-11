package com.kominioai.domain.survey.application.port.input.query

import com.kominioai.domain.survey.domain.valueobject.SurveyId

data class GetSurveyResponsesQuery(val surveyId: SurveyId)
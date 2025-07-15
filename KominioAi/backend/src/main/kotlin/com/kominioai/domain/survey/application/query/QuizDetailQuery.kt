package com.kominioai.domain.survey.application.query

import com.kominioai.domain.survey.domain.model.SurveyId

data class QuizDetailQuery(
    val surveyId: SurveyId
)
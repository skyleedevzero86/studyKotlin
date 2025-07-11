package com.kominioai.domain.survey.application.port.input.command

import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.model.domain.Answer

data class SubmitSurveyResponseCommand(
    val surveyId: SurveyId,
    val respondentId: String?,
    val answers: List<Answer>,
    val ipAddress: String?
)
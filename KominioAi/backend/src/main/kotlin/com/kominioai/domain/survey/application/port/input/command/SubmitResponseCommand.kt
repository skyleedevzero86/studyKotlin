package com.kominioai.domain.survey.application.port.input.command

import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId

data class SubmitResponseCommand(
    val surveyId: SurveyId,
    val respondentId: UserId?,
    val answers: List<AnswerSubmission>
)

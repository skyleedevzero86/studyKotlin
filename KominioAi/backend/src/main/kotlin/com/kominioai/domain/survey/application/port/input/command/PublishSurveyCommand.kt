package com.kominioai.domain.survey.application.port.input.command

import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId

data class PublishSurveyCommand(
    val surveyId: SurveyId,
    val userId: UserId
)
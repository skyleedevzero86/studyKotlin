package com.kominioai.domain.survey.application.port.input.command

import com.kominioai.domain.survey.domain.valueobject.SurveyId

data class PublishSurveyCommand(val surveyId: SurveyId)
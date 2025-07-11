package com.kominioai.domain.survey.application.port.input.command

import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.model.SurveySettings

data class CreateSurveyCommand(
    val title: String,
    val description: String?,
    val createdBy: UserId,
    val settings: SurveySettings
)
package com.kominioai.domain.survey.application.port.input.command

import com.kominioai.domain.survey.domain.valueobject.UserId

data class CreateSurveyCommand(
    val title: String,
    val description: String? = null,
    val createdBy: UserId
)
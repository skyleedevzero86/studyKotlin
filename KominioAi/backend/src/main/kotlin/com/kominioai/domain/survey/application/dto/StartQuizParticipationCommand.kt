package com.kominioai.domain.survey.application.dto

import com.kominioai.domain.survey.domain.model.SurveyId
import com.kominioai.domain.survey.domain.model.UserId

data class StartQuizParticipationCommand(
    val surveyId: SurveyId,
    val participantName: String,
    val participantPhone: String,
    val userId: UserId? = null
)
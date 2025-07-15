package com.kominioai.domain.survey.application.dto

import com.kominioai.domain.survey.domain.model.ParticipationId

data class SubmitQuizParticipationCommand(
    val participationId: ParticipationId
)
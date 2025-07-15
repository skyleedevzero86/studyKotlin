package com.kominioai.domain.survey.application.dto

import com.kominioai.domain.survey.domain.model.ParticipationId

data class SubmitQuizAnswerCommand(
    val participationId: ParticipationId,
    val questionId: String,
    val answer: Any
)
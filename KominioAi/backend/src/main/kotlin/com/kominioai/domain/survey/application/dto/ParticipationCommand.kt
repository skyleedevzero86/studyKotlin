package com.kominioai.domain.survey.application.dto

data class ParticipationCommand(
    val surveyId: String,
    val participant: ParticipantDto,
    val responses: List<QuestionResponseDto>
)
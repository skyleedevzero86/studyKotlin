package com.kominioai.domain.survey.application.dto

import com.kominioai.domain.survey.domain.model.ParticipantInfo
import com.kominioai.domain.survey.domain.model.QuestionResponse
import com.kominioai.domain.survey.domain.model.SurveyId

data class ParticipateQuizCommand(
    val surveyId: SurveyId,
    val participantInfo: ParticipantInfo,
    val responses: List<QuestionResponse>
)
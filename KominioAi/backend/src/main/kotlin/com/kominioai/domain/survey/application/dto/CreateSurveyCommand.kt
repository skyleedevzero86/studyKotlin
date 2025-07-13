package com.kominioai.domain.survey.application.dto

import com.kominioai.domain.survey.domain.model.ParticipantType
import com.kominioai.domain.survey.domain.model.SurveyType
import com.kominioai.domain.survey.domain.model.TimeLimit
import java.time.LocalDateTime

data class CreateSurveyCommand(
    val title: String,
    val author: String,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val surveyType: SurveyType,
    val participantType: ParticipantType,
    val timeLimit: TimeLimit?,
    val questions: List<QuestionDto>
)

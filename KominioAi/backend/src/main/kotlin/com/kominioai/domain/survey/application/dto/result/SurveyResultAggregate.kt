package com.kominioai.domain.survey.application.dto.result

import com.kominioai.domain.survey.domain.model.SurveyId
import com.kominioai.domain.survey.domain.model.QuestionId
import java.time.LocalDateTime

data class SurveyResultAggregate(
    val surveyId: SurveyId,
    val questionResults: List<QuestionResultEntity>,
    val totalParticipants: Int,
    val calculatedAt: LocalDateTime
)
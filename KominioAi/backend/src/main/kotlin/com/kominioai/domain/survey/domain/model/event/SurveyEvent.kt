package com.kominioai.domain.survey.domain.model.event

import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId
import java.time.Instant

sealed class SurveyEvent {
    data class SurveyCreated(
        val surveyId: SurveyId,
        val title: String,
        val createdBy: UserId,
        val createdAt: Instant
    ) : SurveyEvent()

    data class SurveyPublished(
        val surveyId: SurveyId,
        val publishedAt: Instant
    ) : SurveyEvent()

    data class SurveyCompleted(
        val surveyId: SurveyId,
        val responseId: ResponseId,
        val completedAt: Instant
    ) : SurveyEvent()
}
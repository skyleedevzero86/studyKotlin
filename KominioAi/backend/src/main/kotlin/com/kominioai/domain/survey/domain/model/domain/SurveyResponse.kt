package com.kominioai.domain.survey.domain.model.domain

import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import java.time.LocalDateTime

data class SurveyResponse(
    val id: ResponseId,
    val surveyId: SurveyId,
    val respondentId: String?,
    val submittedAt: LocalDateTime,
    val answers: List<Answer>,
    val ipAddress: String?
) {
    fun addAnswer(answer: Answer): SurveyResponse {
        return copy(answers = answers + answer)
    }

    companion object {
        fun create(
            surveyId: SurveyId,
            respondentId: String?,
            answers: List<Answer>,
            ipAddress: String?
        ): SurveyResponse {
            return SurveyResponse(
                id = ResponseId.generate(),
                surveyId = surveyId,
                respondentId = respondentId,
                submittedAt = LocalDateTime.now(),
                answers = answers,
                ipAddress = ipAddress
            )
        }
    }
}
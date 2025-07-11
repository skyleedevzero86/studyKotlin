package com.kominioai.domain.survey.application.port.output

import com.kominioai.domain.survey.domain.model.SurveyResponse
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId

interface SurveyResponseRepository {
    suspend fun save(response: SurveyResponse): SurveyResponse
    suspend fun findById(id: ResponseId): SurveyResponse?
    suspend fun findBySurveyId(surveyId: SurveyId): List<SurveyResponse>
    suspend fun countBySurveyId(surveyId: SurveyId): Long
}
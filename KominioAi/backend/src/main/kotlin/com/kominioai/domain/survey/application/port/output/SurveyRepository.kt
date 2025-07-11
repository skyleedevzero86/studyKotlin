package com.kominioai.domain.survey.application.port.output

import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId

interface SurveyRepository {
    suspend fun save(survey: Survey): Survey
    suspend fun findById(id: SurveyId): Survey?
    suspend fun findByCreatedBy(userId: UserId): List<Survey>
    suspend fun findPublishedSurveys(): List<Survey>
    suspend fun delete(id: SurveyId)
}
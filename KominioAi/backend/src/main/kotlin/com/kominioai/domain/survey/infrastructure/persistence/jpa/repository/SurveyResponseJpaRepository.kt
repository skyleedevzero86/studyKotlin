package com.kominioai.domain.survey.infrastructure.persistence.jpa.repository

import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.SurveyResponse
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import org.springframework.data.jpa.repository.JpaRepository

interface SurveyResponseJpaRepository : JpaRepository<SurveyResponse, String> {
    fun findBySurveyId(surveyId: SurveyId): List<SurveyResponse>
    fun countBySurveyId(surveyId: SurveyId): Long
}
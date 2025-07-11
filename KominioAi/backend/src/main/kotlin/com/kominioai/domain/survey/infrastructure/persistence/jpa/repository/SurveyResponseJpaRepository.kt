package com.kominioai.domain.survey.infrastructure.persistence.jpa.repository

import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.SurveyResponse
import org.springframework.data.jpa.repository.JpaRepository

interface SurveyResponseJpaRepository : JpaRepository<SurveyResponse, String> {
    fun findBySurveyId(surveyId: String): List<SurveyResponse>
    fun countBySurveyId(surveyId: String): Long
}
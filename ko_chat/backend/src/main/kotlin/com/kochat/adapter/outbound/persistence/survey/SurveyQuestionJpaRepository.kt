package com.kochat.adapter.outbound.persistence.survey

import org.springframework.data.jpa.repository.JpaRepository

interface SurveyQuestionJpaRepository : JpaRepository<SurveyQuestionJpaEntity, Long> {
    fun findBySurveyIdOrderByQuestionNoAsc(surveyId: Long): List<SurveyQuestionJpaEntity>

    fun deleteBySurveyId(surveyId: Long)
}

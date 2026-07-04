package com.kochat.adapter.outbound.persistence.survey

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SurveyQuestionJpaRepository : JpaRepository<SurveyQuestionJpaEntity, Long> {
    @Query(
        """
        SELECT q FROM SurveyQuestionJpaEntity q
        WHERE q.survey.id = :surveyId
        ORDER BY q.questionNo ASC
        """,
    )
    fun findBySurveyIdOrderByQuestionNoAsc(@Param("surveyId") surveyId: Long): List<SurveyQuestionJpaEntity>

    @Modifying
    @Query("DELETE FROM SurveyQuestionJpaEntity q WHERE q.survey.id = :surveyId")
    fun deleteBySurveyId(@Param("surveyId") surveyId: Long)
}

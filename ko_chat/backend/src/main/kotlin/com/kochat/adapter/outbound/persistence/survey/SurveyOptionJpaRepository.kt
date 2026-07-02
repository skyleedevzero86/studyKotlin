package com.kochat.adapter.outbound.persistence.survey

import org.springframework.data.jpa.repository.JpaRepository

interface SurveyOptionJpaRepository : JpaRepository<SurveyOptionJpaEntity, Long> {
    fun findByQuestionIdOrderByOptionNoAsc(questionId: Long): List<SurveyOptionJpaEntity>

    fun findByQuestionSurveyIdOrderByQuestionQuestionNoAscOptionNoAsc(surveyId: Long): List<SurveyOptionJpaEntity>

    fun deleteByQuestionId(questionId: Long)
}

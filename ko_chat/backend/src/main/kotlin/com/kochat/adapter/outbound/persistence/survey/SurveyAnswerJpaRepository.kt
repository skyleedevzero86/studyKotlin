package com.kochat.adapter.outbound.persistence.survey

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SurveyAnswerJpaRepository : JpaRepository<SurveyAnswerJpaEntity, Long> {
    fun findBySurveyId(surveyId: Long): List<SurveyAnswerJpaEntity>

    fun findBySurveyIdAndUserId(surveyId: Long, userId: Long): List<SurveyAnswerJpaEntity>

    fun existsBySurveyId(surveyId: Long): Boolean

    fun deleteBySurveyId(surveyId: Long)

    @Query(
        """
        SELECT a.option.id, COUNT(a)
        FROM SurveyAnswerJpaEntity a
        WHERE a.survey.id = :surveyId AND a.option IS NOT NULL
        GROUP BY a.option.id
        """,
    )
    fun countByOptionForSurvey(@Param("surveyId") surveyId: Long): List<Array<Any>>

    @Query(
        """
        SELECT a.question.id, COUNT(DISTINCT a.user.id)
        FROM SurveyAnswerJpaEntity a
        WHERE a.survey.id = :surveyId
        GROUP BY a.question.id
        """,
    )
    fun countRespondentsByQuestion(@Param("surveyId") surveyId: Long): List<Array<Any>>

    @Query(
        """
        SELECT a.survey.chatRoom.id, COUNT(DISTINCT a.user.id)
        FROM SurveyAnswerJpaEntity a
        WHERE (:surveyId IS NULL OR a.survey.id = :surveyId)
        GROUP BY a.survey.chatRoom.id
        """,
    )
    fun countRespondentsByRoom(@Param("surveyId") surveyId: Long?): List<Array<Any>>
}

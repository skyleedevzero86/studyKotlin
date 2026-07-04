package com.kochat.adapter.outbound.persistence.survey

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SurveyAnswerJpaRepository : JpaRepository<SurveyAnswerJpaEntity, Long> {
    @Query("SELECT a FROM SurveyAnswerJpaEntity a WHERE a.survey.id = :surveyId")
    fun findBySurveyId(@Param("surveyId") surveyId: Long): List<SurveyAnswerJpaEntity>

    @Query(
        """
        SELECT a FROM SurveyAnswerJpaEntity a
        WHERE a.survey.id = :surveyId AND a.user.id = :userId
        """,
    )
    fun findBySurveyIdAndUserId(
        @Param("surveyId") surveyId: Long,
        @Param("userId") userId: Long,
    ): List<SurveyAnswerJpaEntity>

    @Query(
        """
        SELECT COUNT(a) > 0
        FROM SurveyAnswerJpaEntity a
        WHERE a.survey.id = :surveyId
        """,
    )
    fun existsBySurveyId(@Param("surveyId") surveyId: Long): Boolean

    @Modifying
    @Query("DELETE FROM SurveyAnswerJpaEntity a WHERE a.survey.id = :surveyId")
    fun deleteBySurveyId(@Param("surveyId") surveyId: Long)

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

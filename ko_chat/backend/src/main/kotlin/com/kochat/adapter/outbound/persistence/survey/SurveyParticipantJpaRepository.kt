package com.kochat.adapter.outbound.persistence.survey

import com.kochat.domain.survey.model.ParticipantStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SurveyParticipantJpaRepository : JpaRepository<SurveyParticipantJpaEntity, Long> {
    @Query(
        """
        SELECT p FROM SurveyParticipantJpaEntity p
        WHERE p.survey.id = :surveyId
        ORDER BY p.assignedAt ASC
        """,
    )
    fun findBySurveyIdOrderByAssignedAtAsc(@Param("surveyId") surveyId: Long): List<SurveyParticipantJpaEntity>

    fun findBySurveyIdAndUserId(surveyId: Long, userId: Long): SurveyParticipantJpaEntity?

    @Query(
        """
        SELECT COUNT(p) > 0
        FROM SurveyParticipantJpaEntity p
        WHERE p.survey.id = :surveyId AND p.user.id = :userId
        """,
    )
    fun existsBySurveyIdAndUserId(
        @Param("surveyId") surveyId: Long,
        @Param("userId") userId: Long,
    ): Boolean

    @Query(
        """
        SELECT COUNT(p) FROM SurveyParticipantJpaEntity p
        WHERE p.survey.id = :surveyId
        """,
    )
    fun countBySurveyId(@Param("surveyId") surveyId: Long): Long

    @Query(
        """
        SELECT COUNT(p) FROM SurveyParticipantJpaEntity p
        WHERE p.survey.id = :surveyId AND p.status = :status
        """,
    )
    fun countBySurveyIdAndStatus(
        @Param("surveyId") surveyId: Long,
        @Param("status") status: ParticipantStatus,
    ): Long

    @Modifying
    @Query("DELETE FROM SurveyParticipantJpaEntity p WHERE p.survey.id = :surveyId")
    fun deleteBySurveyId(@Param("surveyId") surveyId: Long)

    @Query(
        """
        SELECT p.user.id FROM SurveyParticipantJpaEntity p
        WHERE p.survey.id = :surveyId
        """,
    )
    fun findUserIdsBySurveyId(@Param("surveyId") surveyId: Long): List<Long>

    @Query(
        """
        SELECT p FROM SurveyParticipantJpaEntity p
        JOIN FETCH p.survey s
        LEFT JOIN FETCH s.chatRoom
        WHERE p.user.id = :userId
          AND s.status IN (
            com.kochat.domain.survey.model.SurveyStatus.ACTIVE,
            com.kochat.domain.survey.model.SurveyStatus.CLOSED
          )
        ORDER BY p.assignedAt DESC
        """,
    )
    fun findForMySurveysByUserId(@Param("userId") userId: Long): List<SurveyParticipantJpaEntity>
}

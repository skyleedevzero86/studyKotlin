package com.kochat.adapter.outbound.persistence.survey

import com.kochat.domain.survey.model.ParticipantStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SurveyParticipantJpaRepository : JpaRepository<SurveyParticipantJpaEntity, Long> {
    fun findBySurveyIdOrderByAssignedAtAsc(surveyId: Long): List<SurveyParticipantJpaEntity>

    fun findBySurveyIdAndUserId(surveyId: Long, userId: Long): SurveyParticipantJpaEntity?

    fun existsBySurveyIdAndUserId(surveyId: Long, userId: Long): Boolean

    fun countBySurveyId(surveyId: Long): Long

    fun countBySurveyIdAndStatus(surveyId: Long, status: ParticipantStatus): Long

    fun deleteBySurveyId(surveyId: Long)

    @Query(
        """
        SELECT p.user.id FROM SurveyParticipantJpaEntity p
        WHERE p.survey.id = :surveyId
        """,
    )
    fun findUserIdsBySurveyId(@Param("surveyId") surveyId: Long): List<Long>
}

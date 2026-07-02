package com.kochat.adapter.outbound.persistence.survey

import com.kochat.domain.survey.model.SurveyStatus
import com.kochat.domain.survey.model.TargetMode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface SurveyJpaRepository : JpaRepository<SurveyJpaEntity, Long> {
    fun findByChatRoomIdOrderByCreatedAtDesc(chatRoomId: Long): List<SurveyJpaEntity>

    fun findByChatRoomIdOrderByCreatedAtDesc(
        chatRoomId: Long,
        pageable: Pageable,
    ): Page<SurveyJpaEntity>

    fun findByChatRoomIdAndStatusOrderByCreatedAtDesc(
        chatRoomId: Long,
        status: SurveyStatus,
    ): List<SurveyJpaEntity>

    fun findByChatRoomIdAndStatusOrderByCreatedAtDesc(
        chatRoomId: Long,
        status: SurveyStatus,
        pageable: Pageable,
    ): Page<SurveyJpaEntity>

    @Query(
        """
        SELECT s FROM SurveyJpaEntity s
        WHERE (:status IS NULL OR s.status = :status)
          AND (:chatRoomId IS NULL OR s.chatRoom.id = :chatRoomId)
          AND (:targetMode IS NULL OR s.targetMode = :targetMode)
          AND (:title IS NULL OR :title = '' OR LOWER(s.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:from IS NULL OR s.createdAt >= :from)
          AND (:to IS NULL OR s.createdAt <= :to)
        ORDER BY s.createdAt DESC
        """,
    )
    fun findForAdmin(
        @Param("status") status: SurveyStatus?,
        @Param("chatRoomId") chatRoomId: Long?,
        @Param("targetMode") targetMode: TargetMode?,
        @Param("title") title: String?,
        @Param("from") from: LocalDateTime?,
        @Param("to") to: LocalDateTime?,
        pageable: Pageable,
    ): Page<SurveyJpaEntity>
}

package com.kochat.adapter.outbound.persistence.chat

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatRoomJpaRepository : JpaRepository<ChatRoomJpaEntity, Long> {
    @Query(
        """
        SELECT DISTINCT cr FROM ChatRoomJpaEntity cr
        JOIN ChatRoomMemberJpaEntity crm ON cr.id = crm.chatRoom.id
        WHERE crm.user.id = :userId AND crm.isActive = true AND cr.isActive = true
        ORDER BY cr.updatedAt DESC
        """,
    )
    fun findUserChatRooms(userId: Long, pageable: Pageable): Page<ChatRoomJpaEntity>

    fun findByIsActiveTrueOrderByCreatedAtDesc(): List<ChatRoomJpaEntity>

    fun findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(name: String): List<ChatRoomJpaEntity>
}

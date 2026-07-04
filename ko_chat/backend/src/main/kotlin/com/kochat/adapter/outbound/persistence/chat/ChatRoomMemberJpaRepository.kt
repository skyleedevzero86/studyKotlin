package com.kochat.adapter.outbound.persistence.chat

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface ChatRoomMemberJpaRepository : JpaRepository<ChatRoomMemberJpaEntity, Long> {
    fun findByChatRoomIdAndIsActiveTrue(chatRoomId: Long): List<ChatRoomMemberJpaEntity>

    @Query(
        """
        SELECT m FROM ChatRoomMemberJpaEntity m
        JOIN FETCH m.user
        WHERE m.chatRoom.id = :chatRoomId AND m.isActive = true
        """,
        countQuery = "SELECT COUNT(m) FROM ChatRoomMemberJpaEntity m WHERE m.chatRoom.id = :chatRoomId AND m.isActive = true",
    )
    fun findByChatRoomIdAndIsActiveTrue(
        chatRoomId: Long,
        pageable: org.springframework.data.domain.Pageable,
    ): org.springframework.data.domain.Page<ChatRoomMemberJpaEntity>

    fun findByChatRoomIdAndUserIdAndIsActiveTrue(chatRoomId: Long, userId: Long): Optional<ChatRoomMemberJpaEntity>

    fun findByChatRoomIdAndUserId(chatRoomId: Long, userId: Long): Optional<ChatRoomMemberJpaEntity>

    @Query("SELECT COUNT(crm) FROM ChatRoomMemberJpaEntity crm WHERE crm.chatRoom.id = :chatRoomId AND crm.isActive = true")
    fun countActiveMembersInRoom(chatRoomId: Long): Long

    fun existsByChatRoomIdAndUserIdAndIsActiveTrue(chatRoomId: Long, userId: Long): Boolean
}

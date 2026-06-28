package com.kochat.adapter.outbound.persistence.chat

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.Optional

interface ChatRoomMemberJpaRepository : CrudRepository<ChatRoomMemberJpaEntity, Long> {
    fun findByChatRoomIdAndIsActiveTrue(chatRoomId: Long): List<ChatRoomMemberJpaEntity>

    fun findByChatRoomIdAndUserIdAndIsActiveTrue(chatRoomId: Long, userId: Long): Optional<ChatRoomMemberJpaEntity>

    @Query("SELECT COUNT(crm) FROM ChatRoomMemberJpaEntity crm WHERE crm.chatRoom.id = :chatRoomId AND crm.isActive = true")
    fun countActiveMembersInRoom(chatRoomId: Long): Long

    @Modifying
    @Query(
        """
        UPDATE ChatRoomMemberJpaEntity crm
        SET crm.isActive = false, crm.leftAt = CURRENT_TIMESTAMP
        WHERE crm.chatRoom.id = :chatRoomId AND crm.user.id = :userId
        """,
    )
    fun leaveChatRoom(chatRoomId: Long, userId: Long)

    fun existsByChatRoomIdAndUserIdAndIsActiveTrue(chatRoomId: Long, userId: Long): Boolean
}

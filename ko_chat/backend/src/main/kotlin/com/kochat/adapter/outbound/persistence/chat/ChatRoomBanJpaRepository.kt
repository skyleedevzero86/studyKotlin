package com.kochat.adapter.outbound.persistence.chat

import org.springframework.data.jpa.repository.JpaRepository

interface ChatRoomBanJpaRepository : JpaRepository<ChatRoomBanJpaEntity, Long> {
    fun findByChatRoomIdAndUserId(chatRoomId: Long, userId: Long): ChatRoomBanJpaEntity?

    fun existsByChatRoomIdAndUserIdAndIsActiveTrue(chatRoomId: Long, userId: Long): Boolean
}

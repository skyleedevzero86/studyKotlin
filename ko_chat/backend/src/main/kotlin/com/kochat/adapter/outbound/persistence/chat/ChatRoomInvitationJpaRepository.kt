package com.kochat.adapter.outbound.persistence.chat

import com.kochat.domain.chat.model.ChatInvitationStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatRoomInvitationJpaRepository : JpaRepository<ChatRoomInvitationJpaEntity, Long> {
    fun findByChatRoomIdAndInviteeIdAndStatus(
        chatRoomId: Long,
        inviteeId: Long,
        status: ChatInvitationStatus,
    ): ChatRoomInvitationJpaEntity?

    @Query(
        """
        SELECT i FROM ChatRoomInvitationJpaEntity i
        WHERE i.invitee.id = :inviteeId
          AND i.status = :status
          AND i.chatRoom.isActive = true
        ORDER BY i.createdAt DESC
        """,
    )
    fun findByInviteeIdAndStatusOrderByCreatedAtDesc(
        inviteeId: Long,
        status: ChatInvitationStatus,
        pageable: Pageable,
    ): Page<ChatRoomInvitationJpaEntity>

    @Query(
        """
        SELECT i FROM ChatRoomInvitationJpaEntity i
        WHERE i.status = com.kochat.domain.chat.model.ChatInvitationStatus.PENDING
          AND i.chatRoom.type = com.kochat.domain.chat.model.ChatRoomType.DIRECT
          AND i.chatRoom.isActive = true
          AND i.inviter.id = :inviterId
          AND i.invitee.id = :inviteeId
        ORDER BY i.createdAt DESC
        """,
    )
    fun findPendingDirectInvitations(inviterId: Long, inviteeId: Long): List<ChatRoomInvitationJpaEntity>

    fun findByChatRoomIdAndStatus(chatRoomId: Long, status: ChatInvitationStatus): List<ChatRoomInvitationJpaEntity>
}

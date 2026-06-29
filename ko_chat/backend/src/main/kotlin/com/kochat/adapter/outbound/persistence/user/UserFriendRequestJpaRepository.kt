package com.kochat.adapter.outbound.persistence.user

import com.kochat.domain.user.model.FriendRequestStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserFriendRequestJpaRepository : JpaRepository<UserFriendRequestJpaEntity, Long> {
    fun findByRequesterIdAndRecipientIdAndStatus(
        requesterId: Long,
        recipientId: Long,
        status: FriendRequestStatus,
    ): UserFriendRequestJpaEntity?

    fun findByRecipientIdAndStatusOrderByCreatedAtDesc(
        recipientId: Long,
        status: FriendRequestStatus,
    ): List<UserFriendRequestJpaEntity>

    fun findByRequesterIdAndStatusOrderByCreatedAtDesc(
        requesterId: Long,
        status: FriendRequestStatus,
    ): List<UserFriendRequestJpaEntity>

    @Query(
        """
        SELECT fr FROM UserFriendRequestJpaEntity fr
        WHERE fr.recipient.id = :recipientId
          AND fr.status = com.kochat.domain.user.model.FriendRequestStatus.REJECTED
        ORDER BY fr.respondedAt DESC, fr.createdAt DESC
        """,
    )
    fun findRejectedReceivedRequests(recipientId: Long): List<UserFriendRequestJpaEntity>
}

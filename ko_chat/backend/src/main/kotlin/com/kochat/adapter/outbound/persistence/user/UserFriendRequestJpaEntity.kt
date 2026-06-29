package com.kochat.adapter.outbound.persistence.user

import com.kochat.domain.user.model.FriendRequestStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_friend_requests",
    indexes = [
        Index(name = "idx_friend_request_requester", columnList = "requester_id"),
        Index(name = "idx_friend_request_recipient", columnList = "recipient_id"),
        Index(name = "idx_friend_request_status", columnList = "status"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class UserFriendRequestJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    var requester: UserJpaEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    var recipient: UserJpaEntity? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: FriendRequestStatus = FriendRequestStatus.PENDING

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column
    var respondedAt: LocalDateTime? = null
}

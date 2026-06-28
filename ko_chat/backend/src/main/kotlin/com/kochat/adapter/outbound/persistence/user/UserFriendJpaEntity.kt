package com.kochat.adapter.outbound.persistence.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_friends",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["owner_id", "friend_id"]),
    ],
    indexes = [
        Index(name = "idx_user_friend_owner", columnList = "owner_id"),
        Index(name = "idx_user_friend_friend", columnList = "friend_id"),
        Index(name = "idx_user_friend_active", columnList = "is_active"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class UserFriendJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    var owner: UserJpaEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    var friend: UserJpaEntity? = null

    @Column(nullable = false)
    var isActive: Boolean = true

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}

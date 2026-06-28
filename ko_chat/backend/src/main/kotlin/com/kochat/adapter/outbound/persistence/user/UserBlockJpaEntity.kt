package com.kochat.adapter.outbound.persistence.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_blocks",
    indexes = [
        Index(name = "idx_user_block_blocker", columnList = "blocker_id"),
        Index(name = "idx_user_block_blocked", columnList = "blocked_id"),
        Index(name = "idx_user_block_active", columnList = "is_active"),
        Index(name = "idx_user_block_time", columnList = "blocked_at,unblocked_at"),
    ],
)
class UserBlockJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    var blocker: UserJpaEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    var blocked: UserJpaEntity? = null

    @Column(nullable = false)
    var isActive: Boolean = true

    @Column(name = "blocked_at", nullable = false)
    var blockedAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "unblocked_at")
    var unblockedAt: LocalDateTime? = null
}

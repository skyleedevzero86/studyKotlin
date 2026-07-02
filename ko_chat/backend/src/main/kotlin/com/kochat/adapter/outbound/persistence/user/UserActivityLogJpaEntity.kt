package com.kochat.adapter.outbound.persistence.user

import com.kochat.domain.user.model.UserActivityEventType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_activity_logs",
    indexes = [
        Index(name = "idx_user_activity_occurred", columnList = "occurred_at"),
        Index(name = "idx_user_activity_user_occurred", columnList = "user_id, occurred_at"),
        Index(name = "idx_user_activity_type_occurred", columnList = "event_type, occurred_at"),
    ],
)
class UserActivityLogJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    var eventType: UserActivityEventType = UserActivityEventType.JOIN

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "actor_id")
    var actorId: Long? = null
}

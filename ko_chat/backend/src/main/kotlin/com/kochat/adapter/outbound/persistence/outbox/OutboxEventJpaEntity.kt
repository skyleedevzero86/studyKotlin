package com.kochat.adapter.outbound.persistence.outbox

import com.kochat.domain.messaging.model.OutboxEventStatus
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
    name = "outbox_events",
    indexes = [
        Index(name = "idx_outbox_status_created", columnList = "status, created_at"),
        Index(name = "uk_outbox_event_id", columnList = "event_id", unique = true),
    ],
)
class OutboxEventJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "event_id", nullable = false, length = 64, unique = true)
    var eventId: String = ""

    @Column(nullable = false, length = 120)
    var topic: String = ""

    @Column(name = "partition_key", nullable = false, length = 64)
    var partitionKey: String = ""

    @Column(name = "event_type", nullable = false, length = 64)
    var eventType: String = ""

    @Column(nullable = false, columnDefinition = "TEXT")
    var payload: String = ""

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OutboxEventStatus = OutboxEventStatus.PENDING

    @Column(name = "retry_count", nullable = false)
    var retryCount: Int = 0

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "published_at")
    var publishedAt: LocalDateTime? = null

    @Column(name = "last_error", length = 1000)
    var lastError: String? = null
}

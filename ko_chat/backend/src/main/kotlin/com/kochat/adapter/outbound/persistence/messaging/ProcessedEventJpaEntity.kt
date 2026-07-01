package com.kochat.adapter.outbound.persistence.messaging

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "processed_events",
    indexes = [
        Index(name = "uk_processed_event_consumer", columnList = "event_id, consumer_name", unique = true),
    ],
)
class ProcessedEventJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "event_id", nullable = false, length = 64)
    var eventId: String = ""

    @Column(name = "consumer_name", nullable = false, length = 80)
    var consumerName: String = ""

    @Column(name = "processed_at", nullable = false)
    var processedAt: LocalDateTime = LocalDateTime.now()
}

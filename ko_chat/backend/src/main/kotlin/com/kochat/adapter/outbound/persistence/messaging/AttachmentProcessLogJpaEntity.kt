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
    name = "attachment_process_logs",
    indexes = [
        Index(name = "uk_attachment_process_event", columnList = "event_id", unique = true),
        Index(name = "idx_attachment_process_attachment", columnList = "attachment_id"),
    ],
)
class AttachmentProcessLogJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "event_id", nullable = false, length = 64, unique = true)
    var eventId: String = ""

    @Column(name = "attachment_id", nullable = false)
    var attachmentId: Long = 0

    @Column(name = "room_id", nullable = false)
    var roomId: Long = 0

    @Column(name = "message_id", nullable = false)
    var messageId: Long = 0

    @Column(name = "object_key", nullable = false, length = 500)
    var objectKey: String = ""

    @Column(name = "processed_at", nullable = false)
    var processedAt: LocalDateTime = LocalDateTime.now()
}

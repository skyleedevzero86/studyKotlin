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
    name = "chat_audit_logs",
    indexes = [
        Index(name = "uk_audit_event_id", columnList = "event_id", unique = true),
        Index(name = "idx_audit_room_created", columnList = "room_id, created_at"),
    ],
)
class ChatAuditLogJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "event_id", nullable = false, length = 64, unique = true)
    var eventId: String = ""

    @Column(name = "room_id", nullable = false)
    var roomId: Long = 0

    @Column(name = "message_id", nullable = false)
    var messageId: Long = 0

    @Column(name = "event_type", nullable = false, length = 64)
    var eventType: String = ""

    @Column(name = "sender_id", nullable = false)
    var senderId: Long = 0

    @Column(name = "message_type", nullable = false, length = 32)
    var messageType: String = ""

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
}

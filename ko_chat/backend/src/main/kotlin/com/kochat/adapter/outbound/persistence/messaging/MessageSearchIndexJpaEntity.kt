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
    name = "message_search_index",
    indexes = [
        Index(name = "uk_search_message_id", columnList = "message_id", unique = true),
        Index(name = "idx_search_room_created", columnList = "room_id, indexed_at"),
    ],
)
class MessageSearchIndexJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "message_id", nullable = false, unique = true)
    var messageId: Long = 0

    @Column(name = "room_id", nullable = false)
    var roomId: Long = 0

    @Column(name = "sender_id", nullable = false)
    var senderId: Long = 0

    @Column(name = "message_type", nullable = false, length = 32)
    var messageType: String = ""

    @Column(name = "content_preview", nullable = false, length = 500)
    var contentPreview: String = ""

    @Column(name = "indexed_at", nullable = false)
    var indexedAt: LocalDateTime = LocalDateTime.now()
}

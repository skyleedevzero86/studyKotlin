package com.kochat.adapter.outbound.persistence.chat

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "message_attachments",
    indexes = [
        Index(name = "idx_attachment_message_id", columnList = "message_id"),
        Index(name = "idx_attachment_object_key", columnList = "object_key"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class MessageAttachmentJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "message_id")
    var messageId: Long? = null

    @Column(name = "chat_room_id", nullable = false)
    var chatRoomId: Long = 0

    @Column(name = "object_key", nullable = false, length = 500)
    var objectKey: String = ""

    @Column(name = "file_name", nullable = false, length = 255)
    var fileName: String = ""

    @Column(name = "mime_type", nullable = false, length = 120)
    var mimeType: String = ""

    @Column(nullable = false)
    var size: Long = 0

    @Column(nullable = false)
    var milvusIndexed: Boolean = false

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
}

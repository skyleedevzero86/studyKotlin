package com.kochat.adapter.outbound.persistence.chat

import com.kochat.adapter.outbound.persistence.user.UserJpaEntity
import com.kochat.domain.chat.model.MessageType
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
    name = "messages",
    indexes = [
        Index(name = "idx_message_chat_room_id", columnList = "chat_room_id"),
        Index(name = "idx_message_sender_id", columnList = "sender_id"),
        Index(name = "idx_message_created_at", columnList = "created_at"),
        Index(name = "idx_message_room_time", columnList = "chat_room_id,created_at"),
        Index(name = "idx_message_room_sequence", columnList = "chat_room_id,sequence_number"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class MessageJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    var chatRoom: ChatRoomJpaEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    var sender: UserJpaEntity? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var type: MessageType = MessageType.TEXT

    @Column(columnDefinition = "TEXT")
    var content: String? = null

    @Column(nullable = false)
    var isEdited: Boolean = false

    @Column(nullable = false)
    var isDeleted: Boolean = false

    @Column(nullable = false)
    var sequenceNumber: Long = 0

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column
    var editedAt: LocalDateTime? = null
}

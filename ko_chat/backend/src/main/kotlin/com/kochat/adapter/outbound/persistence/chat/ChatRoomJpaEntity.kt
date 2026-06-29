package com.kochat.adapter.outbound.persistence.chat

import com.kochat.adapter.outbound.persistence.user.UserJpaEntity
import com.kochat.domain.chat.model.ChatRoomType
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
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "chat_rooms",
    indexes = [
        Index(name = "idx_chat_room_created_by", columnList = "created_by"),
        Index(name = "idx_chat_room_type", columnList = "type"),
        Index(name = "idx_chat_room_active", columnList = "is_active"),
        Index(name = "idx_chat_room_private", columnList = "is_private"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class ChatRoomJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, length = 100)
    var name: String = ""

    @Column(columnDefinition = "TEXT")
    var description: String? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var type: ChatRoomType = ChatRoomType.GROUP

    @Column(length = 500)
    var imageUrl: String? = null

    @Column(nullable = false)
    var isActive: Boolean = true

    @Column(nullable = false)
    var maxMembers: Int = 100

    @Column(nullable = false)
    var isPrivate: Boolean = false

    @Column(length = 100)
    var passwordHash: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    var createdBy: UserJpaEntity? = null

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime? = null
}

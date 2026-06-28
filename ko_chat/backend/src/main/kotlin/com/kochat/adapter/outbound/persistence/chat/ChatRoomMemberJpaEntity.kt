package com.kochat.adapter.outbound.persistence.chat

import com.kochat.adapter.outbound.persistence.user.UserJpaEntity
import com.kochat.domain.chat.model.MemberRole
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
import jakarta.persistence.UniqueConstraint
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "chat_room_members",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["chat_room_id", "user_id"]),
    ],
    indexes = [
        Index(name = "idx_chat_room_member_user_id", columnList = "user_id"),
        Index(name = "idx_chat_room_member_chat_room_id", columnList = "chat_room_id"),
        Index(name = "idx_chat_room_member_active", columnList = "is_active"),
        Index(name = "idx_chat_room_member_role", columnList = "role"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class ChatRoomMemberJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    var chatRoom: ChatRoomJpaEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserJpaEntity? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: MemberRole = MemberRole.MEMBER

    @Column(nullable = false)
    var isActive: Boolean = true

    @Column
    var lastReadMessageId: Long? = null

    @Column(nullable = false)
    var joinedAt: LocalDateTime = LocalDateTime.now()

    @Column
    var leftAt: LocalDateTime? = null

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
}

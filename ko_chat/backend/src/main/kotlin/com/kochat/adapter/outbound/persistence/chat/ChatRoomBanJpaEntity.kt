package com.kochat.adapter.outbound.persistence.chat

import com.kochat.adapter.outbound.persistence.user.UserJpaEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
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
    name = "chat_room_bans",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["chat_room_id", "user_id"]),
    ],
    indexes = [
        Index(name = "idx_chat_room_ban_room", columnList = "chat_room_id"),
        Index(name = "idx_chat_room_ban_user", columnList = "user_id"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class ChatRoomBanJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    var chatRoom: ChatRoomJpaEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserJpaEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banned_by", nullable = false)
    var bannedBy: UserJpaEntity? = null

    @Column(nullable = false)
    var isActive: Boolean = true

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
}

package com.kochat.adapter.outbound.persistence.chat

import com.kochat.adapter.outbound.persistence.user.UserJpaEntity
import com.kochat.domain.chat.model.ChatInvitationStatus
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
    name = "chat_room_invitations",
    indexes = [
        Index(name = "idx_chat_invitation_room", columnList = "chat_room_id"),
        Index(name = "idx_chat_invitation_invitee", columnList = "invitee_id"),
        Index(name = "idx_chat_invitation_status", columnList = "status"),
    ],
)
@EntityListeners(AuditingEntityListener::class)
class ChatRoomInvitationJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    var chatRoom: ChatRoomJpaEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    var inviter: UserJpaEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_id", nullable = false)
    var invitee: UserJpaEntity? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ChatInvitationStatus = ChatInvitationStatus.PENDING

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column
    var respondedAt: LocalDateTime? = null
}

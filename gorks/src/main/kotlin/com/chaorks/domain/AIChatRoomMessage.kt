package com.chaorks.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime


@Entity
@EntityListeners(AuditingEntityListener::class)
class AIChatRoomMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @CreatedDate
    @Column(updatable = false)
    var createDate: LocalDateTime? = null,

    @LastModifiedDate
    var modifyDate: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    var chatRoom: AIChatRoom? = null,

    @Column(columnDefinition = "LONGTEXT")
    var userMessage: String? = null,

    @Column(columnDefinition = "LONGTEXT")
    var botMessage: String? = null
)

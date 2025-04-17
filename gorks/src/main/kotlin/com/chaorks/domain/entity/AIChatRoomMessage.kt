package com.chaorks.domain.entity

import com.chaorks.global.base.BaseEntity
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener


@Entity
@EntityListeners(AuditingEntityListener::class)
class AIChatRoomMessage(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    var chatRoom: AIChatRoom? = null,

    @Column(columnDefinition = "LONGTEXT")
    var userMessage: String? = null,

    @Column(columnDefinition = "LONGTEXT")
    var botMessage: String? = null
) : BaseEntity(){}

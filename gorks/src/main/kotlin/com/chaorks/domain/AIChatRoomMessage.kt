package com.chaorks.domain

import com.chaorks.global.base.BaseEntity
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime


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

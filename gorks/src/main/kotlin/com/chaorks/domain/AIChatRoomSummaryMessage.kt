package com.chaorks.domain

import com.chaorks.global.base.BaseEntity
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class AIChatRoomSummaryMessage(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    var chatRoom: AIChatRoom,

    @Column(columnDefinition = "LONGTEXT")
    var message: String,

    var startMessageIndex: Int,

    var endMessageIndex: Int
) : BaseEntity() {
    fun getMessageNo() = startMessageIndex + 1
    fun getEndMessageNo() = endMessageIndex + 1
}
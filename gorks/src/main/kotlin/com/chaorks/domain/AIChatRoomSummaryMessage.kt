package com.chaorks.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class AIChatRoomSummaryMessage(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @CreatedDate
    var createDate: LocalDateTime? = null,

    @LastModifiedDate
    var modifyDate: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    var chatRoom: AIChatRoom,

    @Column(columnDefinition = "LONGTEXT")
    var message: String,

    var startMessageIndex: Int,

    var endMessageIndex: Int
) {

    fun getMessageNo() = startMessageIndex + 1

    fun getEndMessageNo() = endMessageIndex + 1
}
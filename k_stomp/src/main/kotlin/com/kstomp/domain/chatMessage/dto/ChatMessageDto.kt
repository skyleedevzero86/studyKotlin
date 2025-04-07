package com.kstomp.domain.chatMessage.dto

import com.kstomp.domain.chatMessage.entity.ChatMessage
import java.time.LocalDateTime

data class ChatMessageDto(
    val id: Long,
    val createDate: LocalDateTime,
    val modifyDate: LocalDateTime,
    val chatRoomId: Long,
    val writerName: String,
    val content: String
) {
    constructor(message: ChatMessage) : this(
        id = message.id,
        createDate = message.createDate,
        modifyDate = message.modifyDate,
        chatRoomId = message.chatRoomId,
        writerName = message.writerName,
        content = message.content
    )
}
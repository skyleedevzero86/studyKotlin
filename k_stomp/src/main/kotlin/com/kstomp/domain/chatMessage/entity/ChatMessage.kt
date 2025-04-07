package com.kstomp.domain.chatMessage.entity

import java.time.LocalDateTime

data class ChatMessage(
    val id: Long,
    val createDate: LocalDateTime,
    val modifyDate: LocalDateTime,
    val chatRoomId: Long,
    val writerName: String,
    val content: String
)
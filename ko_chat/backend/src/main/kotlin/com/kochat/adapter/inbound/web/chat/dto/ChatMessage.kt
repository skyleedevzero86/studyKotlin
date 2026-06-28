package com.kochat.adapter.inbound.web.chat.dto

import com.kochat.domain.chat.model.MessageType
import java.time.LocalDateTime

data class ChatMessage(
    val id: Long,
    val content: String,
    val type: MessageType,
    val senderId: Long,
    val senderName: String,
    val sequenceNumber: Long,
    override val chatRoomId: Long,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
) : WebSocketMessage()

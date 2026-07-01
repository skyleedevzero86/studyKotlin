package com.kochat.domain.messaging.model

import java.time.LocalDateTime

data class ChatMessageSentEvent(
    val eventId: String,
    val eventType: ChatEventType = ChatEventType.CHAT_MESSAGE_SENT,
    val roomId: Long,
    val messageId: Long,
    val sequenceNumber: Long,
    val senderId: Long,
    val messageType: String,
    val createdAt: LocalDateTime,
)

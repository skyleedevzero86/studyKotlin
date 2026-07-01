package com.kochat.adapter.inbound.web.chat.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeName
import com.kochat.domain.chat.model.MessageType
import java.time.LocalDateTime

@JsonTypeName("CHAT_MESSAGE")
data class ChatMessage(
    val id: Long,
    val content: String,
    @get:JsonProperty("messageType")
    val messageType: MessageType,
    val metadata: String? = null,
    val senderId: Long,
    val senderName: String,
    val sequenceNumber: Long,
    override val chatRoomId: Long,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
) : WebSocketMessage()

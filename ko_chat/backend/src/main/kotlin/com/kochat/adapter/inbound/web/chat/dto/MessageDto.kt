package com.kochat.adapter.inbound.web.chat.dto

import com.kochat.domain.chat.model.MessageType
import java.time.LocalDateTime

data class MessageDto(
    val id: Long,
    val chatRoomId: Long,
    val sender: ChatUserDto,
    val type: MessageType,
    val content: String?,
    val isEdited: Boolean,
    val isDeleted: Boolean,
    val createdAt: LocalDateTime,
    val editedAt: LocalDateTime?,
    val sequenceNumber: Long = 0,
)

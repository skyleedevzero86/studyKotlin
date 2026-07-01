package com.kochat.global.application.chat

import com.kochat.domain.chat.model.MessageType

data class PreparedChatMessage(
    val chatRoomId: Long,
    val senderId: Long,
    val type: MessageType,
    val content: String?,
    val metadataJson: String?,
)

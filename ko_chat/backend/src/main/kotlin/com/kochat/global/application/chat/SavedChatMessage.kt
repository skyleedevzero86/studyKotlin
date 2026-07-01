package com.kochat.global.application.chat

import com.kochat.adapter.inbound.web.chat.dto.ChatMessage
import com.kochat.adapter.inbound.web.chat.dto.MessageDto
import com.kochat.adapter.outbound.persistence.chat.MessageAttachmentJpaEntity

data class SavedChatMessage(
    val messageDto: MessageDto,
    val chatMessage: ChatMessage,
    val roomId: Long,
    val senderId: Long,
    val attachment: MessageAttachmentJpaEntity?,
)

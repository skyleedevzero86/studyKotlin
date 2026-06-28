package com.kochat.adapter.outbound.redis

import com.kochat.adapter.inbound.web.chat.dto.ChatMessage
import java.time.LocalDateTime

data class DistributedMessage(
    val id: String,
    val serverId: String,
    val roomId: Long,
    val excludeServerId: String?,
    val timestamp: LocalDateTime,
    val payload: ChatMessage,
)

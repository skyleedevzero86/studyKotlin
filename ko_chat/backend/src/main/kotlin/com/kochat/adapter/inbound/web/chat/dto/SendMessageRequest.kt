package com.kochat.adapter.inbound.web.chat.dto

import com.kochat.domain.chat.model.MessageType
import jakarta.validation.constraints.NotNull

data class SendMessageRequest(
    @field:NotNull(message = "채팅방 ID는 필수입니다")
    val chatRoomId: Long,
    @field:NotNull(message = "메시지 타입은 필수입니다")
    val type: MessageType,
    val content: String?,
)

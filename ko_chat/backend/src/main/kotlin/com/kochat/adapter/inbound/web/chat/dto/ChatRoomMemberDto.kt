package com.kochat.adapter.inbound.web.chat.dto

import com.kochat.domain.chat.model.MemberRole
import java.time.LocalDateTime

data class ChatRoomMemberDto(
    val id: Long,
    val user: ChatUserDto,
    val role: MemberRole,
    val isActive: Boolean,
    val lastReadMessageId: Long?,
    val joinedAt: LocalDateTime,
    val leftAt: LocalDateTime?,
)

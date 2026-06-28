package com.kochat.adapter.inbound.web.user.dto

import com.kochat.adapter.inbound.web.chat.dto.ChatUserDto
import java.time.LocalDateTime

data class UserBlockHistoryDto(
    val id: Long,
    val user: ChatUserDto,
    val blockedAt: LocalDateTime,
    val unblockedAt: LocalDateTime?,
    val isActive: Boolean,
)

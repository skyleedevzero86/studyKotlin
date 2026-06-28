package com.kochat.adapter.inbound.web.chat.dto

import java.time.LocalDateTime

data class ChatUserDto(
    val id: Long,
    val username: String,
    val displayName: String?,
    val profileImageUrl: String? = null,
    val status: String? = null,
    val isActive: Boolean = true,
    val lastSeenAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
)

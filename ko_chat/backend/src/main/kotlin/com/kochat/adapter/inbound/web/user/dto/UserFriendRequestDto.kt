package com.kochat.adapter.inbound.web.user.dto

import com.kochat.adapter.inbound.web.chat.dto.ChatUserDto
import com.kochat.domain.user.model.FriendRequestStatus
import java.time.LocalDateTime

data class UserFriendRequestDto(
    val id: Long,
    val requester: ChatUserDto,
    val recipient: ChatUserDto,
    val status: FriendRequestStatus,
    val createdAt: LocalDateTime,
    val respondedAt: LocalDateTime?,
)

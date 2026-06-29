package com.kochat.adapter.inbound.web.chat.dto

import com.kochat.domain.chat.model.ChatRoomType
import java.time.LocalDateTime

data class ChatRoomDto(
    val id: Long,
    val name: String,
    val description: String?,
    val type: ChatRoomType,
    val imageUrl: String?,
    val isActive: Boolean,
    val maxMembers: Int,
    val memberCount: Int,
    val createdBy: ChatUserDto,
    val createdAt: LocalDateTime,
    val lastMessage: MessageDto?,
    val peerUser: ChatUserDto? = null,
    val unreadCount: Long = 0,
    val isJoined: Boolean = false,
)

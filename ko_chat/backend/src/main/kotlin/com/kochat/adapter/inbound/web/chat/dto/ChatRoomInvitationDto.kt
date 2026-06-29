package com.kochat.adapter.inbound.web.chat.dto

import com.kochat.domain.chat.model.ChatInvitationStatus
import java.time.LocalDateTime

data class ChatRoomInvitationDto(
    val id: Long,
    val chatRoom: ChatRoomDto,
    val inviter: ChatUserDto,
    val invitee: ChatUserDto,
    val status: ChatInvitationStatus,
    val createdAt: LocalDateTime,
    val respondedAt: LocalDateTime?,
)

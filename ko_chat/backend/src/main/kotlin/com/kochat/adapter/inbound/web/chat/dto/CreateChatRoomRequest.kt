package com.kochat.adapter.inbound.web.chat.dto

import com.kochat.domain.chat.model.ChatMediaMode
import com.kochat.domain.chat.model.ChatRoomType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CreateChatRoomRequest(
    @field:NotBlank(message = "채팅방 이름은 필수입니다")
    @field:Size(min = 1, max = 100, message = "채팅방 이름은 1-100자 사이여야 합니다")
    val name: String,
    val description: String?,
    @field:NotNull(message = "채팅방 타입은 필수입니다")
    val type: ChatRoomType,
    val imageUrl: String? = null,
    val maxMembers: Int = 100,
    val isPrivate: Boolean = false,
    val password: String? = null,
    val mediaMode: ChatMediaMode = ChatMediaMode.TEXT,
)

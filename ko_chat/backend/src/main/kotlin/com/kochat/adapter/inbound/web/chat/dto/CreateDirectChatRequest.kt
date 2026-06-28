package com.kochat.adapter.inbound.web.chat.dto

import jakarta.validation.constraints.NotNull

data class CreateDirectChatRequest(
    @field:NotNull(message = "대화 상대 사용자 ID는 필수입니다")
    val targetUserId: Long,
)

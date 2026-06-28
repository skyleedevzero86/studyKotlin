package com.kochat.adapter.inbound.web.chat.dto

import java.time.LocalDateTime

data class ErrorMessage(
    val message: String,
    val code: String? = null,
    override val chatRoomId: Long?,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
) : WebSocketMessage()

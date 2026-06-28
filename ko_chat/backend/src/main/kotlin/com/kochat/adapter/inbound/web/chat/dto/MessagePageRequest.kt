package com.kochat.adapter.inbound.web.chat.dto

import com.kochat.domain.chat.model.MessageDirection

data class MessagePageRequest(
    val chatRoomId: Long,
    val cursor: Long? = null,
    val limit: Int = 50,
    val direction: MessageDirection = MessageDirection.BEFORE,
)

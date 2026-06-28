package com.kochat.adapter.inbound.web.chat.dto

data class MessagePageResponse(
    val messages: List<MessageDto>,
    val nextCursor: Long?,
    val prevCursor: Long?,
    val hasNext: Boolean,
    val hasPrev: Boolean,
)

package com.kochat.adapter.inbound.websocket.webmedia

data class WebMediaObjectMessageContainer(
    val roomId: String,
    val from: String,
    val to: String,
    val type: WebMediaMessageType,
    val messageId: String,
    val message: Any,
)

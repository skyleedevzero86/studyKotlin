package com.kochat.adapter.inbound.websocket.webmedia

data class WebMediaStringMessageContainer(
    val roomId: String,
    val from: String,
    val to: String,
    val type: WebMediaMessageType,
    val messageId: String,
    val message: String,
)

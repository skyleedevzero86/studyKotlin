package com.kochat.adapter.inbound.websocket.webmedia

data class WebMediaUserKickedEventMessage(
    val userId: String,
    val message: String,
)

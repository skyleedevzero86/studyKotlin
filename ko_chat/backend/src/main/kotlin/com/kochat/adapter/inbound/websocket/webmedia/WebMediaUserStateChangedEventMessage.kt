package com.kochat.adapter.inbound.websocket.webmedia

data class WebMediaUserStateChangedEventMessage(
    val userId: String,
    val published: Boolean,
)

package com.kochat.adapter.inbound.websocket.webmedia

data class WebMediaJoinResponseMessage(
    val apiUrl: String,
    val streamUrl: String,
    val roomId: String,
    val user: WebMediaUserMessage,
    val otherUsers: List<WebMediaUserMessage>,
    val maxMembers: Int,
)

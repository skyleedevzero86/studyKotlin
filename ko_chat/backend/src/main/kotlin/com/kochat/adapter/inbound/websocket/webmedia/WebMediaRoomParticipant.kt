package com.kochat.adapter.inbound.websocket.webmedia

import org.springframework.web.socket.WebSocketSession

data class WebMediaRoomParticipant(
    val koUserId: Long,
    val userId: String,
    var published: Boolean,
    val session: WebSocketSession,
)

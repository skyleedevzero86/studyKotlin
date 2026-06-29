package com.kochat.adapter.inbound.websocket.webmedia

data class WebMediaStringMessageContainer(
    val roomId: String,
    val from: String,
    val to: String,
    val type: WebMediaMessageType,
    val messageId: String,
    val message: String,
)

data class WebMediaObjectMessageContainer(
    val roomId: String,
    val from: String,
    val to: String,
    val type: WebMediaMessageType,
    val messageId: String,
    val message: Any,
)

data class WebMediaJoinRequestMessage(
    val roomId: String,
)

data class WebMediaUserMessage(
    val userId: String,
    val published: Boolean,
)

data class WebMediaJoinResponseMessage(
    val apiUrl: String,
    val streamUrl: String,
    val roomId: String,
    val user: WebMediaUserMessage,
    val otherUsers: List<WebMediaUserMessage>,
    val maxMembers: Int,
)

data class WebMediaErrorResponseMessage(
    val errorCode: WebMediaErrorCode,
    val message: String,
)

data class WebMediaUserJoinedEventMessage(
    val user: WebMediaUserMessage,
)

data class WebMediaUserLeftEventMessage(
    val userId: String,
)

data class WebMediaUserStateChangedEventMessage(
    val userId: String,
    val published: Boolean,
)

data class WebMediaUserPublishedChangeReportMessage(
    val published: Boolean,
)

data class WebMediaUserKickedEventMessage(
    val userId: String,
    val message: String,
)

data class WebMediaRoomParticipant(
    val koUserId: Long,
    val userId: String,
    var published: Boolean,
    val session: org.springframework.web.socket.WebSocketSession,
)

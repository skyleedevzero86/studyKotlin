package com.kochat.adapter.inbound.websocket.webmedia

enum class WebMediaMessageType {
    JoinRequest,
    JoinResponse,
    ErrorResponse,
    UserJoinedEvent,
    UserLeftEvent,
    UserStateChangedEvent,
    UserPublishedChangeReport,
    StreamRepublishedReport,
    UserStreamRepublishedEvent,
    UserKickedEvent,
}

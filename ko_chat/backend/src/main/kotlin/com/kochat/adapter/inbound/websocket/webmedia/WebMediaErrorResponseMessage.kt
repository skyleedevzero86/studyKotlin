package com.kochat.adapter.inbound.websocket.webmedia

data class WebMediaErrorResponseMessage(
    val errorCode: WebMediaErrorCode,
    val message: String,
)

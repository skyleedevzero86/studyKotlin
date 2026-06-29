package com.kochat.adapter.inbound.websocket.webmedia

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

@Component
class WebMediaMessageSender(
    private val objectMapper: ObjectMapper,
) {
    fun sendTransactionMessage(
        session: WebSocketSession,
        roomId: String,
        to: String,
        messageId: String,
        type: WebMediaMessageType,
        message: Any,
    ) {
        val container = WebMediaObjectMessageContainer(
            roomId = roomId,
            from = FROM,
            to = to,
            type = type,
            messageId = messageId,
            message = message,
        )
        session.sendMessage(TextMessage(objectMapper.writeValueAsString(container)))
    }

    fun sendEventMessage(
        session: WebSocketSession,
        roomId: String,
        type: WebMediaMessageType,
        message: Any,
    ) {
        val container = WebMediaObjectMessageContainer(
            roomId = roomId,
            from = FROM,
            to = ALL,
            type = type,
            messageId = NO_TRANSACTION_MESSAGE_ID,
            message = message,
        )
        session.sendMessage(TextMessage(objectMapper.writeValueAsString(container)))
    }

    companion object {
        private const val FROM = "webmedia-ws"
        private const val ALL = "all"
        private const val NO_TRANSACTION_MESSAGE_ID = "none"
    }
}

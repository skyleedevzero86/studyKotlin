package com.kochat.adapter.inbound.websocket.webmedia

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class WebMediaWebSocketHandler(
    private val objectMapper: ObjectMapper,
    private val webMediaSessionRegistry: WebMediaSessionRegistry,
) : TextWebSocketHandler() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        webMediaSessionRegistry.handleConnectionClosed(session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val koUserId = session.attributes["userId"] as? Long
        if (koUserId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE)
            return
        }

        try {
            val container = objectMapper.readValue(message.payload, WebMediaStringMessageContainer::class.java)
            val agent = when (container.type) {
                WebMediaMessageType.JoinRequest -> webMediaSessionRegistry.getOrCreateAgent(container.roomId)
                else -> webMediaSessionRegistry.getAgent(container.roomId)
            } ?: run {
                session.close(CloseStatus.BAD_DATA)
                return
            }

            agent.handleMessage(
                session = session,
                koUserId = koUserId,
                messageId = container.messageId,
                type = container.type,
                messageStr = container.message,
            )
        } catch (e: Exception) {
            logger.warn("WebMedia 메시지 처리 실패", e)
            session.close(CloseStatus.SERVER_ERROR)
        }
    }
}

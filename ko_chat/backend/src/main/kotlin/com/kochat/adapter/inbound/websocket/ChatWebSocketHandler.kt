package com.kochat.adapter.inbound.websocket

import com.kochat.adapter.inbound.web.chat.dto.ErrorMessage
import com.kochat.adapter.inbound.web.chat.dto.SendMessageRequest
import com.kochat.domain.chat.model.MessageType
import com.kochat.domain.chat.service.ChatService
import com.kochat.adapter.outbound.websocket.WebSocketSessionManager
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import java.io.IOException

@Component
class ChatWebSocketHandler(
    private val sessionManager: WebSocketSessionManager,
    private val chatService: ChatService,
    @Qualifier("distributedObjectMapper") private val objectMapper: ObjectMapper,
) : WebSocketHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = getUserIdFromSession(session)

        if (userId != null) {
            sessionManager.addSession(userId, session)
            try {
                loadUserChatRooms(userId)
            } catch (e: Exception) {
                logger.error("사용자 채팅방 목록을 불러오는 중 오류가 발생했습니다", e)
            }
        }
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val userId = getUserIdFromSession(session) ?: return

        try {
            when (message) {
                is TextMessage -> handleTextMessage(session, userId, message.payload)
                else -> logger.warn("지원하지 않는 메시지 타입입니다: ${message.javaClass.name}")
            }
        } catch (e: Exception) {
            logger.warn("메시지 처리 중 오류가 발생했습니다", e)
            sendErrorMessage(session, "메시지 처리 에러")
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        val userId = getUserIdFromSession(session)

        if (exception !is java.io.EOFException) {
            logger.error("사용자($userId) WebSocket 전송 오류", exception)
        }

        if (userId != null) {
            sessionManager.removeSession(userId, session)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        val userId = getUserIdFromSession(session)
        if (userId != null) {
            sessionManager.removeSession(userId, session)
        }
    }

    override fun supportsPartialMessages(): Boolean = false

    private fun getUserIdFromSession(session: WebSocketSession): Long? =
        session.attributes["userId"] as? Long

    private fun loadUserChatRooms(userId: Long) {
        val chatRooms = chatService.getChatRooms(userId, PageRequest.of(0, 100))
        chatRooms.content.forEach { room ->
            sessionManager.joinRoom(userId, room.id)
        }
    }

    private fun sendErrorMessage(session: WebSocketSession, errorMessage: String, errorCode: String? = null) {
        try {
            val error = ErrorMessage(
                chatRoomId = null,
                message = errorMessage,
                code = errorCode,
            )
            val json = objectMapper.writeValueAsString(error)
            session.sendMessage(TextMessage(json))
        } catch (e: IOException) {
            logger.error("에러 메시지 전송에 실패했습니다", e)
        }
    }

    private fun extractMessageType(payload: String): String? =
        try {
            objectMapper.readTree(payload).get("type")?.asText()
        } catch (e: Exception) {
            null
        }

    private fun handleTextMessage(session: WebSocketSession, userId: Long, payload: String) {
        try {
            val messageType = extractMessageType(payload)

            when (messageType) {
                "SEND_MESSAGE" -> {
                    val jsonNode = objectMapper.readTree(payload)
                    val chatRoomId = jsonNode.get("chatRoomId")?.asLong()
                        ?: throw IllegalArgumentException("채팅방 ID는 필수입니다")
                    val messageTypeText = jsonNode.get("messageType")?.asText()
                        ?: throw IllegalArgumentException("메시지 타입은 필수입니다")
                    val content = jsonNode.get("content")?.asText()
                    val metadata = jsonNode.get("metadata")?.let { node ->
                        if (node.isTextual) node.asText() else node.toString()
                    }

                    val sendMessageRequest = SendMessageRequest(
                        chatRoomId = chatRoomId,
                        type = MessageType.valueOf(messageTypeText),
                        content = content,
                        metadata = metadata,
                    )

                    chatService.sendMessage(sendMessageRequest, userId)
                }
                else -> {
                    sendErrorMessage(session, "알 수 없는 메시지 타입입니다: $messageType", "알_수_없는_메시지_타입")
                }
            }
        } catch (e: Exception) {
            logger.error("사용자($userId) WebSocket 메시지 파싱 오류: ${e.message}", e)
            sendErrorMessage(session, "메시지 형식이 올바르지 않습니다.", "잘못된_메시지_형식")
        }
    }
}

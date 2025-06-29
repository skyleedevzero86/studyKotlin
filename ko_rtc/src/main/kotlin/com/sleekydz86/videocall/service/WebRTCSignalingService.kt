package com.sleekydz86.videocall.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.sleekydz86.videocall.dto.SignalingMessage
import com.sleekydz86.videocall.enums.MessageType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap

@Service
class WebRTCSignalingService(
    private val objectMapper: ObjectMapper,
    private val callRoomService: CallRoomService
) {

    // 각 방별 시그널링 메시지 스트림
    private val roomSignals = ConcurrentHashMap<String, Sinks.Many<SignalingMessage>>()

    // 세션별 룸 매핑
    private val sessionRoomMapping = ConcurrentHashMap<String, String>()

    fun handleWebSocketConnection(session: WebSocketSession): Mono<Void> {
        val roomId = session.handshakeInfo.uri.query?.split("=")?.get(1) ?: ""
        val userId = session.attributes["userId"] as? String ?: "anonymous"
        val userName = session.attributes["userName"] as? String ?: "Unknown"

        sessionRoomMapping[session.id] = roomId

        // 방에 참가
        return callRoomService.joinRoom(roomId, userId, userName, session.id)
            .flatMap { participant ->
                // 다른 참가자들에게 새 사용자 참가 알림
                val joinMessage = SignalingMessage(
                    type = MessageType.USER_JOINED,
                    roomId = roomId,
                    fromUserId = userId,
                    data = mapOf("userName" to userName)
                )

                broadcastToRoom(roomId, joinMessage)

                // WebSocket 메시지 처리
                val input = session.receive()
                    .map { it.payloadAsText }
                    .map { objectMapper.readValue(it, SignalingMessage::class.java) }
                    .doOnNext { message -> handleSignalingMessage(message) }
                    .then()

                val output = getRoomSignalStream(roomId)
                    .filter { it.toUserId == null || it.toUserId == userId }
                    .map { objectMapper.writeValueAsString(it) }
                    .map { session.textMessage(it) }
                    .let { session.send(it) }

                Mono.zip(input, output).then()
            }
            .doFinally {
                // 연결 종료 시 정리
                handleDisconnection(session.id)
            }
    }

    private fun handleSignalingMessage(message: SignalingMessage) {
        when (message.type) {
            MessageType.OFFER, MessageType.ANSWER, MessageType.ICE_CANDIDATE -> {
                // P2P 연결을 위한 시그널링 메시지 전달
                broadcastToRoom(message.roomId, message)
            }
            MessageType.LEAVE_ROOM -> {
                handleLeaveRoom(message.roomId, message.fromUserId)
            }
            else -> {
                // 기타 메시지 처리
                broadcastToRoom(message.roomId, message)
            }
        }
    }

    private fun handleLeaveRoom(roomId: String, userId: String) {
        val leaveMessage = SignalingMessage(
            type = MessageType.USER_LEFT,
            roomId = roomId,
            fromUserId = userId
        )
        broadcastToRoom(roomId, leaveMessage)
    }

    private fun handleDisconnection(sessionId: String) {
        val roomId = sessionRoomMapping[sessionId]
        if (roomId != null) {
            callRoomService.leaveRoom(roomId, sessionId).subscribe()
            sessionRoomMapping.remove(sessionId)

            val leaveMessage = SignalingMessage(
                type = MessageType.USER_LEFT,
                roomId = roomId,
                fromUserId = sessionId
            )
            broadcastToRoom(roomId, leaveMessage)
        }
    }

    private fun broadcastToRoom(roomId: String, message: SignalingMessage) {
        val sink = roomSignals.computeIfAbsent(roomId) {
            Sinks.many().multicast().onBackpressureBuffer()
        }
        sink.tryEmitNext(message)
    }

    private fun getRoomSignalStream(roomId: String): Flux<SignalingMessage> {
        val sink = roomSignals.computeIfAbsent(roomId) {
            Sinks.many().multicast().onBackpressureBuffer()
        }
        return sink.asFlux()
    }
}
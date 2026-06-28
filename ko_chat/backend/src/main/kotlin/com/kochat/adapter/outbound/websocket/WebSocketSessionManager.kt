package com.kochat.adapter.outbound.websocket

import com.kochat.adapter.inbound.web.chat.dto.ChatMessage
import com.kochat.adapter.outbound.persistence.chat.ChatRoomMemberJpaRepository
import com.kochat.adapter.outbound.redis.RedisMessageBroker
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Service
class WebSocketSessionManager(
    private val redisTemplate: RedisTemplate<String, String>,
    @Qualifier("distributedObjectMapper") private val objectMapper: ObjectMapper,
    private val redisMessageBroker: RedisMessageBroker,
    private val chatRoomMemberJpaRepository: ChatRoomMemberJpaRepository,
) {
    private val logger = LoggerFactory.getLogger(WebSocketSessionManager::class.java)
    private val userSession = ConcurrentHashMap<Long, MutableSet<WebSocketSession>>()
    private val serverRoomsKeyPrefix = "chat:server:rooms:"

    @PostConstruct
    fun initialize() {
        redisMessageBroker.setLocalMessageHandler { roomId, msg ->
            sendMessageToLocalRoom(roomId, msg)
        }
    }

    fun addSession(userId: Long, session: WebSocketSession) {
        userSession.computeIfAbsent(userId) { mutableSetOf() }.add(session)
    }

    fun removeSession(userId: Long, session: WebSocketSession) {
        userSession[userId]?.remove(session)

        if (userSession[userId]?.isEmpty() == true) {
            userSession.remove(userId)

            val totalConnectedUsers = userSession.values.sumOf { sessions ->
                sessions.count { it.isOpen }
            }

            if (totalConnectedUsers == 0) {
                val serverId = redisMessageBroker.getServerId()
                val serverRoomKey = "$serverRoomsKeyPrefix$serverId"
                val subscribedRooms = redisTemplate.opsForSet().members(serverRoomKey) ?: emptySet()

                subscribedRooms.forEach { roomIdStr ->
                    val roomId = roomIdStr.toLongOrNull()
                    if (roomId != null) {
                        redisMessageBroker.unsubscribeFromRoom(roomId)
                    }
                }

                redisTemplate.delete(serverRoomKey)
            }
        }
    }

    fun joinRoom(userId: Long, roomId: Long) {
        val serverId = redisMessageBroker.getServerId()
        val serverRoomKey = "$serverRoomsKeyPrefix$serverId"
        val wasAlreadySubscribed = redisTemplate.opsForSet().isMember(serverRoomKey, roomId.toString()) == true

        if (!wasAlreadySubscribed) {
            redisMessageBroker.subscribeToRoom(roomId)
        }

        redisTemplate.opsForSet().add(serverRoomKey, roomId.toString())
        logger.debug("사용자($userId)가 채팅방($roomId)에 참여했습니다. 서버: $serverId")
    }

    fun sendMessageToLocalRoom(roomId: Long, message: ChatMessage, excludeUserId: Long? = null) {
        val json = objectMapper.writeValueAsString(message)

        userSession.forEach { (userId, sessions) ->
            if (userId != excludeUserId) {
                val isMember = chatRoomMemberJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(roomId, userId)

                if (isMember) {
                    val closedSessions = mutableSetOf<WebSocketSession>()

                    sessions.forEach { session ->
                        if (session.isOpen) {
                            try {
                                session.sendMessage(TextMessage(json))
                            } catch (e: Exception) {
                                logger.error("로컬 WebSocket 세션으로 메시지 전송에 실패했습니다", e)
                                closedSessions.add(session)
                            }
                        } else {
                            closedSessions.add(session)
                        }
                    }

                    if (closedSessions.isNotEmpty()) {
                        sessions.removeAll(closedSessions)
                    }
                }
            }
        }
    }

    fun isUserOnlineLocally(userId: Long): Boolean {
        val sessions = userSession[userId] ?: return false
        val openSessions = sessions.filter { it.isOpen }

        if (openSessions.size != sessions.size) {
            val closedSessions = sessions.filter { !it.isOpen }
            sessions.removeAll(closedSessions)
            if (sessions.isEmpty()) {
                userSession.remove(userId)
            }
        }

        return openSessions.isNotEmpty()
    }
}

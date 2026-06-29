package com.kochat.adapter.inbound.websocket.webmedia

import com.fasterxml.jackson.databind.ObjectMapper
import com.kochat.adapter.outbound.persistence.chat.ChatRoomBanJpaRepository
import com.kochat.adapter.outbound.persistence.chat.ChatRoomJpaRepository
import com.kochat.adapter.outbound.persistence.chat.ChatRoomMemberJpaRepository
import com.kochat.global.config.WebMediaProperties
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession

@Component
class WebMediaSessionRegistry(
    private val objectMapper: ObjectMapper,
    private val messageSender: WebMediaMessageSender,
    private val chatRoomJpaRepository: ChatRoomJpaRepository,
    private val chatRoomMemberJpaRepository: ChatRoomMemberJpaRepository,
    private val chatRoomBanJpaRepository: ChatRoomBanJpaRepository,
    private val webMediaProperties: WebMediaProperties,
) {
    private val lockObj = Any()
    private val agentMap = mutableMapOf<String, WebMediaRoomAgent>()

    fun getOrCreateAgent(roomId: String): WebMediaRoomAgent = synchronized(lockObj) {
        agentMap.getOrPut(roomId) {
            WebMediaRoomAgent(
                objectMapper = objectMapper,
                messageSender = messageSender,
                chatRoomJpaRepository = chatRoomJpaRepository,
                chatRoomMemberJpaRepository = chatRoomMemberJpaRepository,
                chatRoomBanJpaRepository = chatRoomBanJpaRepository,
                webMediaProperties = webMediaProperties,
                roomId = roomId,
            )
        }
    }

    fun getAgent(roomId: String): WebMediaRoomAgent? = synchronized(lockObj) {
        agentMap[roomId]
    }

    fun handleConnectionClosed(session: WebSocketSession) {
        synchronized(lockObj) {
            val emptyRooms = mutableListOf<String>()
            agentMap.values.forEach { agent ->
                agent.handleUserLeft(session)
                if (agent.getUserCount() == 0) {
                    emptyRooms.add(agent.roomId)
                }
            }
            emptyRooms.forEach { agentMap.remove(it) }
        }
    }

    fun disconnectUser(roomId: Long, koUserId: Long) {
        synchronized(lockObj) {
            agentMap[roomId.toString()]?.disconnectUser(koUserId, kicked = false)
            cleanupEmptyAgentsLocked()
        }
    }

    fun kickUser(roomId: Long, koUserId: Long) {
        synchronized(lockObj) {
            agentMap[roomId.toString()]?.disconnectUser(koUserId, kicked = true)
            cleanupEmptyAgentsLocked()
        }
    }

    private fun cleanupEmptyAgentsLocked() {
        val emptyRooms = agentMap.filterValues { it.getUserCount() == 0 }.keys
        emptyRooms.forEach { agentMap.remove(it) }
    }
}

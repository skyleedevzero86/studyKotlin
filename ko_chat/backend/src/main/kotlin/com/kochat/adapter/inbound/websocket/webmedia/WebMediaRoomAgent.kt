package com.kochat.adapter.inbound.websocket.webmedia

import com.fasterxml.jackson.databind.ObjectMapper
import com.kochat.adapter.outbound.persistence.chat.ChatRoomBanJpaRepository
import com.kochat.adapter.outbound.persistence.chat.ChatRoomJpaRepository
import com.kochat.adapter.outbound.persistence.chat.ChatRoomMemberJpaRepository
import com.kochat.domain.chat.model.ChatMediaMode
import com.kochat.global.config.WebMediaProperties
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession

class WebMediaRoomAgent(
    private val objectMapper: ObjectMapper,
    private val messageSender: WebMediaMessageSender,
    private val chatRoomJpaRepository: ChatRoomJpaRepository,
    private val chatRoomMemberJpaRepository: ChatRoomMemberJpaRepository,
    private val chatRoomBanJpaRepository: ChatRoomBanJpaRepository,
    private val webMediaProperties: WebMediaProperties,
    val roomId: String,
) {
    private val lockObj = Any()
    private val participants = LinkedHashMap<String, WebMediaRoomParticipant>()

    fun getUserCount(): Int = synchronized(lockObj) { participants.size }

    fun handleUserLeft(session: WebSocketSession) {
        synchronized(lockObj) {
            val participant = participants.remove(session.id) ?: return
            val event = WebMediaUserLeftEventMessage(userId = participant.userId)
            broadcastEvent(WebMediaMessageType.UserLeftEvent, event, excludeSessionId = session.id)
        }
    }

    fun disconnectUser(koUserId: Long, kicked: Boolean) {
        synchronized(lockObj) {
            val targets = participants.values.filter { it.koUserId == koUserId }.toList()
            targets.forEach { participant ->
                participants.remove(participant.session.id)
                if (kicked) {
                    messageSender.sendEventMessage(
                        session = participant.session,
                        roomId = roomId,
                        type = WebMediaMessageType.UserKickedEvent,
                        message = WebMediaUserKickedEventMessage(
                            userId = participant.userId,
                            message = "채팅방에서 내보졌습니다.",
                        ),
                    )
                }
                participant.session.close(CloseStatus.NORMAL)
            }
            if (!kicked && targets.isNotEmpty()) {
                val event = WebMediaUserLeftEventMessage(userId = koUserId.toString())
                broadcastEvent(WebMediaMessageType.UserLeftEvent, event)
            }
        }
    }

    fun handleMessage(
        session: WebSocketSession,
        koUserId: Long,
        messageId: String,
        type: WebMediaMessageType,
        messageStr: String,
    ) {
        synchronized(lockObj) {
            when (type) {
                WebMediaMessageType.JoinRequest -> {
                    val message = objectMapper.readValue(messageStr, WebMediaJoinRequestMessage::class.java)
                    handleUserJoined(session, koUserId, messageId, message)
                }
                WebMediaMessageType.UserPublishedChangeReport -> {
                    val message = objectMapper.readValue(messageStr, WebMediaUserPublishedChangeReportMessage::class.java)
                    handleUserPublishedChangeReport(session, koUserId, message, messageId)
                }
                WebMediaMessageType.StreamRepublishedReport -> {
                    objectMapper.readValue(messageStr, WebMediaStreamRepublishedReportMessage::class.java)
                    handleStreamRepublishedReport(session, koUserId)
                }
                else -> sendError(session, messageId, WebMediaErrorCode.BadRequest, "잘못된 요청입니다")
            }
        }
    }

    private fun handleUserJoined(
        session: WebSocketSession,
        koUserId: Long,
        messageId: String,
        message: WebMediaJoinRequestMessage,
    ) {
        if (roomId != message.roomId) {
            sendError(session, messageId, WebMediaErrorCode.BadRequest, "잘못된 요청입니다")
            return
        }

        val chatRoomId = roomId.toLongOrNull()
        if (chatRoomId == null) {
            sendError(session, messageId, WebMediaErrorCode.BadRequest, "잘못된 요청입니다")
            return
        }

        val chatRoom = chatRoomJpaRepository.findById(chatRoomId).orElse(null)
        if (chatRoom == null || !chatRoom.isActive) {
            sendError(session, messageId, WebMediaErrorCode.BadRequest, "채팅방을 찾을 수 없습니다")
            return
        }

        if (chatRoom.mediaMode != ChatMediaMode.WEBRTC) {
            sendError(session, messageId, WebMediaErrorCode.NotWebRtcRoom, "WebRTC 채팅방이 아닙니다")
            return
        }

        if (!canParticipate(chatRoomId, koUserId)) {
            sendError(session, messageId, WebMediaErrorCode.NotMember, "채팅방에 참여할 수 없습니다")
            return
        }

        removeDuplicateSessions(koUserId, excludeSessionId = session.id)

        val existing = participants[session.id]
        if (existing != null) {
            sendJoinResponse(session, existing, messageId, chatRoom.maxMembers)
            return
        }

        if (participants.size >= chatRoom.maxMembers) {
            sendError(session, messageId, WebMediaErrorCode.TooManyUsers, "방에 인원이 너무 많습니다")
            return
        }

        val mediaUserId = koUserId.toString()
        val participant = WebMediaRoomParticipant(
            koUserId = koUserId,
            userId = mediaUserId,
            published = false,
            session = session,
        )
        participants[session.id] = participant

        sendJoinResponse(session, participant, messageId, chatRoom.maxMembers)

        val joinedEvent = WebMediaUserJoinedEventMessage(
            user = WebMediaUserMessage(userId = participant.userId, published = false),
        )
        broadcastEvent(WebMediaMessageType.UserJoinedEvent, joinedEvent, excludeSessionId = session.id)
    }

    private fun canParticipate(chatRoomId: Long, koUserId: Long): Boolean {
        if (chatRoomBanJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(chatRoomId, koUserId)) {
            return false
        }
        return chatRoomMemberJpaRepository.existsByChatRoomIdAndUserIdAndIsActiveTrue(chatRoomId, koUserId)
    }

    private fun removeDuplicateSessions(koUserId: Long, excludeSessionId: String) {
        participants.values
            .filter { it.koUserId == koUserId && it.session.id != excludeSessionId }
            .forEach { participant ->
                participants.remove(participant.session.id)
                participant.session.close(CloseStatus.NORMAL)
            }
    }

    private fun sendJoinResponse(
        session: WebSocketSession,
        participant: WebMediaRoomParticipant,
        messageId: String,
        maxMembers: Int,
    ) {
        val otherUsers = participants.values
            .filter { it.session.id != session.id }
            .map { WebMediaUserMessage(userId = it.userId, published = it.published) }

        val response = WebMediaJoinResponseMessage(
            apiUrl = webMediaProperties.apiUrl,
            streamUrl = webMediaProperties.streamUrl,
            roomId = roomId,
            user = WebMediaUserMessage(userId = participant.userId, published = participant.published),
            otherUsers = otherUsers,
            maxMembers = maxMembers,
        )

        messageSender.sendTransactionMessage(
            session = session,
            roomId = roomId,
            to = participant.userId,
            messageId = messageId,
            type = WebMediaMessageType.JoinResponse,
            message = response,
        )
    }

    private fun handleUserPublishedChangeReport(
        session: WebSocketSession,
        koUserId: Long,
        message: WebMediaUserPublishedChangeReportMessage,
        messageId: String,
    ) {
        val participant = participants[session.id] ?: return
        val chatRoomId = roomId.toLongOrNull() ?: return
        if (!canParticipate(chatRoomId, koUserId)) {
            disconnectUser(koUserId, kicked = true)
            return
        }
        if (participant.published == message.published) {
            return
        }

        participant.published = message.published
        val event = WebMediaUserStateChangedEventMessage(
            userId = participant.userId,
            published = message.published,
        )
        broadcastEvent(WebMediaMessageType.UserStateChangedEvent, event, excludeSessionId = session.id)
    }

    private fun handleStreamRepublishedReport(
        session: WebSocketSession,
        koUserId: Long,
    ) {
        val participant = participants[session.id] ?: return
        val chatRoomId = roomId.toLongOrNull() ?: return
        if (!canParticipate(chatRoomId, koUserId) || !participant.published) {
            return
        }

        val event = WebMediaUserStreamRepublishedEventMessage(userId = participant.userId)
        broadcastEvent(WebMediaMessageType.UserStreamRepublishedEvent, event, excludeSessionId = session.id)
    }

    private fun broadcastEvent(
        type: WebMediaMessageType,
        message: Any,
        excludeSessionId: String? = null,
    ) {
        participants.values
            .filter { excludeSessionId == null || it.session.id != excludeSessionId }
            .forEach { participant ->
                messageSender.sendEventMessage(
                    session = participant.session,
                    roomId = roomId,
                    type = type,
                    message = message,
                )
            }
    }

    private fun sendError(
        session: WebSocketSession,
        messageId: String,
        errorCode: WebMediaErrorCode,
        message: String,
    ) {
        messageSender.sendTransactionMessage(
            session = session,
            roomId = roomId,
            to = "guest",
            messageId = messageId,
            type = WebMediaMessageType.ErrorResponse,
            message = WebMediaErrorResponseMessage(errorCode = errorCode, message = message),
        )
    }
}

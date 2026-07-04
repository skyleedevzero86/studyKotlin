package com.kochat.global.application.chat

import com.kochat.adapter.inbound.web.chat.dto.MessageDto
import com.kochat.adapter.outbound.persistence.chat.ChatRoomMemberJpaRepository
import org.springframework.stereotype.Service

@Service
class MessageReadReceiptService(
    private val chatRoomMemberJpaRepository: ChatRoomMemberJpaRepository,
) {
    fun memberReadMap(roomId: Long): Map<Long, Long?> =
        chatRoomMemberJpaRepository.findByChatRoomIdAndIsActiveTrue(roomId)
            .mapNotNull { member ->
                val userId = member.user?.id ?: return@mapNotNull null
                userId to member.lastReadMessageId
            }
            .toMap()

    fun unreadCountFor(
        messageId: Long,
        senderId: Long,
        viewerId: Long,
        readMap: Map<Long, Long?>,
    ): Int? {
        if (senderId != viewerId) {
            return null
        }
        val count = readMap.count { (userId, lastRead) ->
            userId != viewerId && (lastRead == null || lastRead < messageId)
        }
        return count.takeIf { it > 0 }
    }

    fun enrich(message: MessageDto, viewerId: Long, readMap: Map<Long, Long?>): MessageDto =
        message.copy(
            unreadMemberCount = unreadCountFor(
                messageId = message.id,
                senderId = message.sender.id,
                viewerId = viewerId,
                readMap = readMap,
            ),
        )
}

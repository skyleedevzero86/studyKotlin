package com.kochat.global.application.chat

import com.kochat.adapter.outbound.persistence.chat.ChatRoomMemberJpaEntity
import com.kochat.adapter.outbound.persistence.chat.ChatRoomMemberJpaRepository
import com.kochat.adapter.outbound.persistence.chat.MessageJpaRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class ChatUnreadCountService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val chatRoomMemberJpaRepository: ChatRoomMemberJpaRepository,
    private val messageJpaRepository: MessageJpaRepository,
) {
    private val keyPrefix = "chat:unread"

    fun getUnreadCount(
        chatRoomId: Long,
        viewerUserId: Long,
        viewerMember: ChatRoomMemberJpaEntity,
        viewerIsAdmin: Boolean,
    ): Long {
        val key = unreadKey(chatRoomId, viewerUserId)
        val cached = redisTemplate.opsForValue().get(key)
        if (cached != null) {
            return cached.toLongOrNull()?.coerceAtLeast(0) ?: 0L
        }

        val dbCount = countFromDatabase(chatRoomId, viewerUserId, viewerMember, viewerIsAdmin)
        redisTemplate.opsForValue().set(key, dbCount.toString())
        return dbCount
    }

    fun resetUnread(chatRoomId: Long, userId: Long) {
        redisTemplate.opsForValue().set(unreadKey(chatRoomId, userId), "0")
    }

    fun incrementUnreadForRoomMembers(chatRoomId: Long, senderId: Long) {
        val members = chatRoomMemberJpaRepository.findByChatRoomIdAndIsActiveTrue(chatRoomId)
        members.forEach { member ->
            val userId = member.user?.id ?: return@forEach
            if (userId == senderId) {
                return@forEach
            }
            incrementUnread(chatRoomId, userId, member)
        }
    }

    private fun incrementUnread(chatRoomId: Long, userId: Long, member: ChatRoomMemberJpaEntity) {
        val key = unreadKey(chatRoomId, userId)
        val cached = redisTemplate.opsForValue().get(key)
        if (cached != null) {
            redisTemplate.opsForValue().increment(key)
            return
        }

        val base = countFromDatabase(chatRoomId, userId, member, viewerIsAdmin = false)
        redisTemplate.opsForValue().set(key, (base + 1).toString())
    }

    private fun countFromDatabase(
        chatRoomId: Long,
        viewerUserId: Long,
        viewerMember: ChatRoomMemberJpaEntity,
        viewerIsAdmin: Boolean,
    ): Long =
        messageJpaRepository.countUnreadVisibleMessages(
            chatRoomId = chatRoomId,
            viewerUserId = viewerUserId,
            joinedAt = viewerMember.joinedAt,
            lastReadMessageId = viewerMember.lastReadMessageId,
            viewerIsAdmin = viewerIsAdmin,
        )

    private fun unreadKey(chatRoomId: Long, userId: Long): String = "$keyPrefix:$chatRoomId:$userId"
}

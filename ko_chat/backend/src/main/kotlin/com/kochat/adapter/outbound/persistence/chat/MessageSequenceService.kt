package com.kochat.adapter.outbound.persistence.chat

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class MessageSequenceService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val messageJpaRepository: MessageJpaRepository,
) {
    private val prefix = "chat:sequence"

    fun getNextSequence(chatRoomId: Long): Long {
        val key = "$prefix:$chatRoomId"
        return try {
            redisTemplate.opsForValue().increment(key) ?: fallbackSequence(chatRoomId)
        } catch (exception: Exception) {
            fallbackSequence(chatRoomId)
        }
    }

    private fun fallbackSequence(chatRoomId: Long): Long =
        messageJpaRepository.findMaxSequenceNumber(chatRoomId) + 1
}

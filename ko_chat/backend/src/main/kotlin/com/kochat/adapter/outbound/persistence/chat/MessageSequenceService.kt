package com.kochat.adapter.outbound.persistence.chat

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class MessageSequenceService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val messageJpaRepository: MessageJpaRepository,
) {
    fun getNextSequence(chatRoomId: Long): Long {
        val key = sequenceKey(chatRoomId)
        return try {
            redisTemplate.opsForValue().increment(key) ?: fallbackSequence(chatRoomId)
        } catch (exception: Exception) {
            fallbackSequence(chatRoomId)
        }
    }

    fun syncSequenceFromDatabase(chatRoomId: Long) {
        val key = sequenceKey(chatRoomId)
        try {
            val maxSequence = messageJpaRepository.findMaxSequenceNumber(chatRoomId)
            if (maxSequence > 0) {
                redisTemplate.opsForValue().setIfAbsent(key, maxSequence.toString())
            }
        } catch (_: Exception) {
        }
    }

    private fun sequenceKey(chatRoomId: Long): String = "chat:room:$chatRoomId:seq"

    private fun fallbackSequence(chatRoomId: Long): Long =
        messageJpaRepository.findMaxSequenceNumber(chatRoomId) + 1
}

package com.azure.domain.service

import com.alibaba.fastjson2.JSON
import com.azure.domain.dto.ChatMessage
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * 채팅 메시지 서비스 클래스
 */
@Service
class ChatHistoryService(private val redisTemplate: StringRedisTemplate) {

    // 채팅 메시지 저장
    fun saveMessage(userId: String, message: ChatMessage) {
        redisTemplate.opsForList().rightPush(userId, JSON.toJSONString(message))
        redisTemplate.expire(userId, Duration.ofHours(1))
    }

    // 채팅 히스토리 조회
    fun getHistory(userId: String): List<ChatMessage> {
        val rawMessages = redisTemplate.opsForList().range(userId, 0, -1) ?: emptyList()
        val chatHistory = rawMessages
            .map { JSON.parseObject(it, ChatMessage::class.java) }
            .toMutableList() // 중요: 수정 가능하게 만듦

        var tokenCount = chatHistory.sumOf { it.content.length }

        if (tokenCount <= 3800) {
            return chatHistory
        }

        // 오래된 메시지부터 제거하며 tokenCount 맞추기
        val iterator = chatHistory.iterator()
        while (tokenCount > 3800 && iterator.hasNext()) {
            val messageToRemove = iterator.next()
            tokenCount -= messageToRemove.content.length
            iterator.remove()

            // Redis에서도 해당 메시지 삭제
            redisTemplate.opsForList().remove(userId, 1, JSON.toJSONString(messageToRemove))
        }

        return chatHistory
    }


    // 채팅 히스토리 삭제
    fun clearHistory(userId: String) {
        redisTemplate.delete(userId)
    }
}
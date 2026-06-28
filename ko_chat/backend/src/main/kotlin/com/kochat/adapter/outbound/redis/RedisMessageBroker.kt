package com.kochat.adapter.outbound.redis

import com.kochat.adapter.inbound.web.chat.dto.ChatMessage
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class RedisMessageBroker(
    private val redisTemplate: RedisTemplate<String, String>,
    private val messageListenerContainer: RedisMessageListenerContainer,
    @Qualifier("distributedObjectMapper") private val objectMapper: ObjectMapper,
) : org.springframework.data.redis.connection.MessageListener {
    private val logger = LoggerFactory.getLogger(RedisMessageBroker::class.java)
    private val serverId = System.getenv("HOSTNAME") ?: "server-${System.currentTimeMillis()}"
    private val processedMessages = ConcurrentHashMap<String, Long>()
    private val subscribeRooms = ConcurrentHashMap.newKeySet<Long>()
    private var localMessageHandler: ((Long, ChatMessage) -> Unit)? = null

    fun getServerId(): String = serverId

    @PostConstruct
    fun initialize() {
        Thread {
            try {
                Thread.sleep(30000)
                cleanUpProcessedMessages()
            } catch (e: Exception) {
                logger.error("Redis 브로커 정리 작업 중 오류가 발생했습니다", e)
            }
        }.apply {
            isDaemon = true
            name = "redis-broker-cleanup"
            start()
        }
    }

    @PreDestroy
    fun cleanup() {
        subscribeRooms.forEach { roomId ->
            unsubscribeFromRoom(roomId)
        }
    }

    fun setLocalMessageHandler(handler: (Long, ChatMessage) -> Unit) {
        localMessageHandler = handler
    }

    fun subscribeToRoom(roomId: Long) {
        if (subscribeRooms.add(roomId)) {
            val topic = ChannelTopic("chat.room.$roomId")
            messageListenerContainer.addMessageListener(this, topic)
            logger.info("채팅방($roomId) Redis 구독을 시작했습니다")
        }
    }

    fun unsubscribeFromRoom(roomId: Long) {
        if (subscribeRooms.remove(roomId)) {
            val topic = ChannelTopic("chat.room.$roomId")
            messageListenerContainer.removeMessageListener(this, topic)
            logger.info("채팅방($roomId) Redis 구독을 해제했습니다")
        }
    }

    fun broadcastToRoom(roomId: Long, message: ChatMessage, excludeServerId: String? = null) {
        try {
            val distributedMessage = DistributedMessage(
                id = "$serverId-${System.currentTimeMillis()}-${System.nanoTime()}",
                serverId = serverId,
                roomId = roomId,
                excludeServerId = excludeServerId,
                timestamp = LocalDateTime.now(),
                payload = message,
            )
            val json = objectMapper.writeValueAsString(distributedMessage)
            redisTemplate.convertAndSend("chat.room.$roomId", json)
        } catch (e: Exception) {
            logger.error("채팅방($roomId) Redis 브로드캐스트 중 오류가 발생했습니다", e)
        }
    }

    override fun onMessage(message: Message, pattern: ByteArray?) {
        try {
            val json = String(message.body)
            val distributedMessage = objectMapper.readValue(json, DistributedMessage::class.java)

            if (distributedMessage.excludeServerId == serverId) {
                return
            }

            if (processedMessages.containsKey(distributedMessage.id)) {
                return
            }

            localMessageHandler?.invoke(distributedMessage.roomId, distributedMessage.payload)
            processedMessages[distributedMessage.id] = System.currentTimeMillis()

            if (processedMessages.size > 10000) {
                val oldestEntries = processedMessages.entries
                    .sortedBy { it.value }
                    .take(processedMessages.size - 10000)
                oldestEntries.forEach { processedMessages.remove(it.key) }
            }
        } catch (e: Exception) {
            logger.error("Redis 메시지 처리 중 오류가 발생했습니다", e)
        }
    }

    private fun cleanUpProcessedMessages() {
        val now = System.currentTimeMillis()
        val expiredKeys = processedMessages.filter { (_, time) ->
            now - time > 60000
        }.keys
        expiredKeys.forEach { processedMessages.remove(it) }
    }
}

package com.sleekydz86.rag.infrastructure.external

import com.sleekydz86.rag.infrastructure.external.sse.SSEMsgType
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Component
class RedisBasedSSEServer(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val redisMessageListenerContainer: RedisMessageListenerContainer
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val connections = ConcurrentHashMap<String, SseEmitter>()
    private val scheduledExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    init {
        setupRedisListener()
        startHeartbeat()
    }

    fun addConnection(userId: String, emitter: SseEmitter) {
        connections[userId] = emitter

        redisTemplate.opsForValue().set("sse:connection:$userId", "connected", java.time.Duration.ofMinutes(30))

        sendMsg(userId, "connected", SSEMsgType.ADD)

        logger.info("SSE 연결 추가: $userId, 총 연결 수: ${connections.size}")
    }

    fun sendMsg(userId: String, message: String, type: SSEMsgType) {
        try {

            val channel = "sse:$userId"
            val messageData = mapOf(
                "type" to type.name,
                "message" to message,
                "timestamp" to System.currentTimeMillis()
            )

            redisTemplate.convertAndSend(channel, messageData)

            val historyKey = "sse:history:$userId"
            redisTemplate.opsForList().leftPush(historyKey, messageData)
            redisTemplate.opsForList().trim(historyKey, 0, 99)

            logger.debug("Redis Pub/Sub으로 메시지 전송: $userId -> $message")

        } catch (e: Exception) {
            logger.error("Redis 메시지 전송 실패: $userId", e)

            sendDirectSSE(userId, message, type)
        }
    }

    fun close(userId: String) {
        connections.remove(userId)?.let { emitter ->
            try {
                emitter.complete()

                redisTemplate.delete("sse:connection:$userId")
                logger.info("SSE 연결 종료: $userId")
            } catch (e: Exception) {
                logger.error("SSE 연결 종료 실패: $userId", e)
            }
        }
    }

    fun getConnectionCount(): Int = connections.size

    fun getActiveUsers(): List<String> = connections.keys.toList()

    private fun setupRedisListener() {
        connections.keys.forEach { userId ->
            val topic = ChannelTopic("sse:$userId")
            redisMessageListenerContainer.addMessageListener(
                object : org.springframework.data.redis.connection.MessageListener {
                    override fun onMessage(message: org.springframework.data.redis.connection.Message, pattern: ByteArray?) {
                        handleRedisMessage(userId, message)
                    }
                },
                topic
            )
        }
    }

    private fun handleRedisMessage(userId: String, message: Any) {
        connections[userId]?.let { emitter ->
            try {
                // Redis 메시지를 SSE로 변환하여 전송
                val messageData = message as? Map<*, *>
                val type = messageData?.get("type") as? String ?: "add"
                val content = messageData?.get("message") as? String ?: ""

                val sseEvent = SseEmitter.event()
                    .name(type.lowercase())
                    .data(content)
                    .build()

                emitter.send(sseEvent)

            } catch (e: Exception) {
                logger.error("Redis 메시지 처리 실패: $userId", e)
                close(userId)
            }
        }
    }

    private fun sendDirectSSE(userId: String, message: String, type: SSEMsgType) {
        connections[userId]?.let { emitter ->
            try {
                val sseEvent = SseEmitter.event()
                    .name(type.name.lowercase())
                    .data(message)
                    .build()

                emitter.send(sseEvent)

            } catch (e: Exception) {
                logger.error("직접 SSE 전송 실패: $userId", e)
                close(userId)
            }
        }
    }

    private fun startHeartbeat() {
        scheduledExecutor.scheduleAtFixedRate({
            connections.keys.forEach { userId ->
                try {
                    sendMsg(userId, "heartbeat", SSEMsgType.ADD)

                    if (!redisTemplate.hasKey("sse:connection:$userId")) {
                        logger.warn("Redis 연결 상태 누락: $userId")
                        close(userId)
                    }
                } catch (e: Exception) {
                    logger.error("하트비트 전송 실패: $userId", e)
                }
            }
        }, 30, 30, TimeUnit.SECONDS)
    }

    fun cleanup() {
        scheduledExecutor.shutdown()
        connections.clear()
    }
}
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
class HybridSSEServer(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val redisMessageListenerContainer: RedisMessageListenerContainer
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val connections = ConcurrentHashMap<String, SseEmitter>()
    private val scheduledExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(3)

    init {
        setupHybridSystem()
        startMonitoring()
    }

    private fun setupHybridSystem() {
        setupPubSubListener()
        setupStreamsListener()
        setupConnectionManager()
    }

    private fun setupPubSubListener() {
        if (connections.isNotEmpty()) {
            connections.keys.forEach { userId ->
                setupUserChannel(userId)
            }
        }
    }

    private fun setupStreamsListener() {
        val systemTopic = "sse:system"
        logger.info("Streams 리스너 설정 완료: $systemTopic")
    }

    private fun setupConnectionManager() {
        scheduledExecutor.scheduleAtFixedRate({
            updateConnectionStatus()
        }, 10, 10, TimeUnit.SECONDS)
    }

    fun addConnection(userId: String, emitter: SseEmitter) {
        connections[userId] = emitter

        val connectionInfo = mapOf(
            "status" to "connected",
            "timestamp" to System.currentTimeMillis().toString(),
            "lastActivity" to System.currentTimeMillis().toString(),
            "userAgent" to "web-client"
        )

        redisTemplate.opsForHash<String, String>().putAll("sse:connections:$userId", connectionInfo)

        setupUserChannel(userId)

        logger.info("하이브리드 SSE 연결 추가: $userId")
    }

    fun sendMsg(userId: String, message: String, type: SSEMsgType) {
        sendViaPubSub(userId, message, type)
        saveToStreams(userId, message, type)
        saveMessageMetadata(userId, message, type)
    }

    private fun sendViaPubSub(userId: String, message: String, type: SSEMsgType) {
        val channel = "sse:user:$userId"
        val messageData = mapOf(
            "type" to type.name,
            "message" to message,
            "timestamp" to System.currentTimeMillis(),
            "delivery" to "pubsub"
        )

        redisTemplate.convertAndSend(channel, messageData)
    }


    private fun saveToStreams(userId: String, message: String, type: SSEMsgType) {
        val listKey = "sse:messages:$userId"

        val messageData = mapOf(
            "type" to type.name,
            "message" to message,
            "timestamp" to System.currentTimeMillis().toString(),
            "delivery" to "list"
        )

        try {

            redisTemplate.opsForList().leftPush(listKey, messageData)
            redisTemplate.opsForList().trim(listKey, 0, 99)

            logger.debug("메시지 List 저장 완료: $listKey")
        } catch (e: Exception) {
            logger.warn("List 저장 실패: $listKey", e)
        }
    }

    private fun saveMessageMetadata(userId: String, message: String, type: SSEMsgType) {
        val metadataKey = "sse:metadata:$userId"
        val metadata = mapOf(
            "lastMessage" to message,
            "lastMessageType" to type.name,
            "lastMessageTime" to System.currentTimeMillis().toString(),
            "messageCount" to incrementMessageCount(userId).toString()
        )

        redisTemplate.opsForHash<String, String>().putAll(metadataKey, metadata)
    }

    private fun incrementMessageCount(userId: String): Long {
        val key = "sse:count:$userId"
        return redisTemplate.opsForValue().increment(key) ?: 1L
    }

    private fun updateConnectionStatus() {
        connections.keys.forEach { userId ->
            try {
                val statusKey = "sse:connections:$userId"
                val lastActivity = redisTemplate.opsForHash<String, String>().get(statusKey, "lastActivity")

                if (lastActivity != null) {
                    val lastActivityTime = lastActivity.toLongOrNull() ?: 0L
                    val currentTime = System.currentTimeMillis()

                    if (currentTime - lastActivityTime > 300000) {
                        logger.warn("비활성 연결 감지: $userId")

                        redisTemplate.opsForHash<String, String>().put(statusKey, "status", "idle")
                    }
                }
            } catch (e: Exception) {
                logger.error("연결 상태 업데이트 실패: $userId", e)
            }
        }
    }

    private fun setupUserChannel(userId: String) {
        val topic = ChannelTopic("sse:user:$userId")
        redisMessageListenerContainer.addMessageListener(
            object : org.springframework.data.redis.connection.MessageListener {
                override fun onMessage(message: org.springframework.data.redis.connection.Message, pattern: ByteArray?) {
                    handleUserMessage(userId, message)
                }
            },
            topic
        )
    }

    private fun handleUserMessage(userId: String, message: org.springframework.data.redis.connection.Message) {
        connections[userId]?.let { emitter ->
            try {
                val messageData = parseMessage(message)
                val sseEvent = SseEmitter.event()
                    .name(messageData["type"] as? String ?: "add")
                    .data(messageData["message"] as? String ?: "")
                    .build()

                emitter.send(sseEvent)
                updateUserActivity(userId)

            } catch (e: Exception) {
                logger.error("사용자 메시지 처리 실패: $userId", e)
                close(userId)
            }
        }
    }

    private fun parseMessage(message: org.springframework.data.redis.connection.Message): Map<String, Any> {
        return try {
            mapOf(
                "type" to "add",
                "message" to String(message.body)
            )
        } catch (e: Exception) {
            mapOf("type" to "error", "message" to "메시지 파싱 실패")
        }
    }

    private fun updateUserActivity(userId: String) {
        val statusKey = "sse:connections:$userId"
        redisTemplate.opsForHash<String, String>().put(statusKey, "lastActivity", System.currentTimeMillis().toString())
    }

    private fun startMonitoring() {
        scheduledExecutor.scheduleAtFixedRate({
            monitorSystemHealth()
        }, 60, 60, TimeUnit.SECONDS)
    }

    private fun monitorSystemHealth() {
        try {
            val totalConnections = connections.size
            val activeConnections = getActiveConnectionCount()
            val memoryUsage = getRedisMemoryUsage()

            logger.info("시스템 상태: 연결 ${totalConnections}개, 활성 ${activeConnections}개, 메모리 ${memoryUsage}MB")

            if (totalConnections > 1000) {
                logger.warn("연결 수가 많습니다: $totalConnections")
            }

            if (memoryUsage > 100) {
                logger.warn("Redis 메모리 사용량이 높습니다: ${memoryUsage}MB")
            }

        } catch (e: Exception) {
            logger.error("시스템 모니터링 실패", e)
        }
    }

    private fun getActiveConnectionCount(): Int {
        return try {
            val pattern = "sse:connections:*"
            val keys = redisTemplate.keys(pattern)

            keys?.count { key ->
                val status = redisTemplate.opsForHash<String, String>().get(key, "status")
                status == "connected"
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun getRedisMemoryUsage(): Long {
        return try {
            val info = redisTemplate.connectionFactory?.connection?.info("memory")

            val usedMemoryStr = info?.get("used_memory")?.toString() ?: "0"
            val usedMemoryLong = usedMemoryStr.toLongOrNull() ?: 0L
            usedMemoryLong / 1024 / 1024
        } catch (e: Exception) {
            0L
        }
    }

    fun close(userId: String) {
        connections.remove(userId)?.let { emitter ->
            try {
                emitter.complete()

                redisTemplate.delete("sse:connections:$userId")
                redisTemplate.delete("sse:health:$userId")

                logger.info("하이브리드 SSE 연결 종료: $userId")
            } catch (e: Exception) {
                logger.error("SSE 연결 종료 실패: $userId", e)
            }
        }
    }

    fun getConnectionCount(): Int = connections.size

    fun getActiveUsers(): List<String> = connections.keys.toList()

    fun cleanup() {
        scheduledExecutor.shutdown()
        connections.clear()

        try {
            val pattern = "sse:*"
            val keys = redisTemplate.keys(pattern)
            keys?.forEach { key ->
                redisTemplate.delete(key)
            }
        } catch (e: Exception) {
            logger.error("Redis 정리 실패", e)
        }
    }
}
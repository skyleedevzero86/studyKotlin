package com.sleekydz86.rag.infrastructure.external

import com.sleekydz86.rag.infrastructure.external.sse.SSEMsgType
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Component
class RedisStreamsSSEServer(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val connections = ConcurrentHashMap<String, SseEmitter>()
    private val scheduledExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(2)

    init {
        startHealthCheck()
        startMessageProcessor()
    }

    fun addConnection(userId: String, emitter: SseEmitter) {
        connections[userId] = emitter

        val connectionEvent = mapOf(
            "type" to "connection",
            "userId" to userId,
            "timestamp" to System.currentTimeMillis().toString(),
            "status" to "connected"
        )

        try {
            redisTemplate.opsForHash<String, String>().putAll("sse:connections:$userId", connectionEvent)
            redisTemplate.opsForSet().add("sse:active_connections", userId)
        } catch (e: Exception) {
            logger.warn("연결 이벤트 저장 실패", e)
        }

        createUserMessageList(userId)

        logger.info("SSE 연결 추가: $userId, 총 연결 수: ${connections.size}")
    }

    fun sendMsg(userId: String, message: String, type: SSEMsgType) {
        try {
            val messageData = mapOf(
                "type" to type.name,
                "message" to message,
                "userId" to userId,
                "timestamp" to System.currentTimeMillis().toString(),
                "sequence" to getNextSequence(userId).toString()
            )

            val listKey = "sse:messages:$userId"

            redisTemplate.opsForList().leftPush(listKey, messageData)
            redisTemplate.opsForList().trim(listKey, 0, 999)
            sendDirectSSE(userId, message, type)

            logger.debug("Redis List 메시지 저장: $userId -> $listKey")
        } catch (e: Exception) {
            logger.error("Redis List 메시지 저장 실패: $userId", e)
            sendDirectSSE(userId, message, type)
        }
    }

    fun broadcastMessage(message: String, type: SSEMsgType = SSEMsgType.ADD) {
        val broadcastData = mapOf(
            "type" to type.name,
            "message" to message,
            "timestamp" to System.currentTimeMillis().toString(),
            "broadcast" to "true"
        )

        try {
            redisTemplate.opsForList().leftPush("sse:broadcast_messages", broadcastData)
            redisTemplate.opsForList().trim("sse:broadcast_messages", 0, 99)
        } catch (e: Exception) {
            logger.warn("브로드캐스트 저장 실패", e)
        }

        connections.keys.forEach { userId ->
            sendMsg(userId, message, type)
        }

        logger.info("브로드캐스트 메시지 전송: $message")
    }

    fun getMessageHistory(userId: String, limit: Int = 50): List<Map<String, String>> {
        return try {
            val listKey = "sse:messages:$userId"
            val messages = redisTemplate.opsForList()
                .range(listKey, 0, (limit - 1).toLong())
                ?.mapNotNull { item ->
                    when (item) {
                        is Map<*, *> -> item.mapKeys { it.key.toString() }.mapValues { it.value.toString() }
                        else -> null
                    }
                } ?: emptyList()

            messages.reversed()
        } catch (e: Exception) {
            logger.error("메시지 히스토리 조회 실패: $userId", e)
            emptyList()
        }
    }

    fun getSystemStats(): Map<String, Any> {
        return try {
            val totalConnections = connections.size
            val activeConnections = getActiveConnectionCount()
            val messageCounts = getMessageCounts()

            mapOf(
                "totalConnections" to totalConnections,
                "activeConnections" to activeConnections,
                "messageCounts" to messageCounts,
                "redisMemoryUsage" to getRedisMemoryUsage(),
                "uptime" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            logger.error("시스템 통계 조회 실패", e)
            emptyMap()
        }
    }

    private fun createUserMessageList(userId: String) {
        val listKey = "sse:messages:$userId"
        if (!redisTemplate.hasKey(listKey)) {
            try {
                val initMessage = mapOf(
                    "type" to "list_created",
                    "userId" to userId,
                    "timestamp" to System.currentTimeMillis().toString()
                )
                redisTemplate.opsForList().leftPush(listKey, initMessage)
            } catch (e: Exception) {
                logger.warn("사용자 메시지 리스트 생성 실패: $userId", e)
            }
        }
    }

    private fun getNextSequence(userId: String): Long {
        val key = "sse:sequence:$userId"
        return redisTemplate.opsForValue().increment(key) ?: 1L
    }

    private fun getActiveConnectionCount(): Int {
        return try {
            val activeConnections = redisTemplate.opsForSet().members("sse:active_connections")
            activeConnections?.size ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun getMessageCounts(): Map<String, Long> {
        return try {
            val pattern = "sse:messages:*"
            val keys = redisTemplate.keys(pattern) ?: emptySet()

            keys.associate { key ->
                val count = redisTemplate.opsForList().size(key) ?: 0L
                key.toString() to count
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun getRedisMemoryUsage(): Long {
        return try {
            val connection = redisTemplate.connectionFactory?.connection
            val info = connection?.info("memory")
            connection?.close()

            val usedMemoryStr = info?.get("used_memory")?.toString() ?: "0"
            val usedMemoryLong = usedMemoryStr.toLongOrNull() ?: 0L
            usedMemoryLong / 1024 / 1024
        } catch (e: Exception) {
            0L
        }
    }

    private fun sendDirectSSE(userId: String, message: String, type: SSEMsgType) {
        connections[userId]?.let { emitter ->
            try {
                val sseEvent = SseEmitter.event()
                    .name(type.name.lowercase())
                    .data(message)

                emitter.send(sseEvent)
            } catch (e: Exception) {
                logger.error("직접 SSE 전송 실패: $userId", e)
                close(userId)
            }
        }
    }

    private fun startHealthCheck() {
        scheduledExecutor.scheduleAtFixedRate({
            connections.keys.forEach { userId ->
                try {
                    val healthKey = "sse:health:$userId"
                    redisTemplate.opsForValue().set(healthKey, "alive", java.time.Duration.ofMinutes(5))
                    val connectionKey = "sse:connections:$userId"
                    if (!redisTemplate.hasKey(connectionKey)) {
                        logger.warn("Redis 연결 상태 누락: $userId")
                        close(userId)
                    }
                } catch (e: Exception) {
                    logger.error("헬스체크 실패: $userId", e)
                }
            }
        }, 30, 30, TimeUnit.SECONDS)
    }

    private fun startMessageProcessor() {
        scheduledExecutor.scheduleAtFixedRate({
            try {
                cleanupDeadConnections()
            } catch (e: Exception) {
                logger.error("메시지 프로세서 오류", e)
            }
        }, 10, 10, TimeUnit.SECONDS)
    }

    private fun cleanupDeadConnections() {
        val deadConnections = mutableListOf<String>()

        connections.forEach { (userId, emitter) ->
            try {
                if (!isConnectionAlive(emitter)) {
                    deadConnections.add(userId)
                }
            } catch (e: Exception) {
                deadConnections.add(userId)
            }
        }

        deadConnections.forEach { userId ->
            close(userId)
            logger.info("죽은 연결 정리: $userId")
        }
    }

    private fun isConnectionAlive(emitter: SseEmitter): Boolean {
        return try {
            emitter.send(SseEmitter.event().comment("heartbeat"))
            true
        } catch (e: Exception) {
            logger.debug("연결 상태 확인 실패: ${e.message}")
            false
        }
    }

    fun close(userId: String) {
        connections.remove(userId)?.let { emitter ->
            try {
                emitter.complete()
                redisTemplate.delete("sse:connections:$userId")
                redisTemplate.delete("sse:health:$userId")
                redisTemplate.opsForSet().remove("sse:active_connections", userId)

                val disconnectEvent = mapOf(
                    "type" to "disconnection",
                    "userId" to userId,
                    "timestamp" to System.currentTimeMillis().toString(),
                    "status" to "disconnected"
                )

                try {
                    redisTemplate.opsForHash<String, String>().putAll("sse:disconnections:$userId", disconnectEvent)
                } catch (e: Exception) {
                    logger.warn("연결 해제 이벤트 저장 실패", e)
                }

                logger.info("SSE 연결 종료: $userId")
            } catch (e: Exception) {
                logger.error("SSE 연결 종료 실패: $userId", e)
            }
        }
    }

    fun getConnectionCount(): Int = connections.size

    fun getActiveUsers(): Set<String> = connections.keys.toSet()

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
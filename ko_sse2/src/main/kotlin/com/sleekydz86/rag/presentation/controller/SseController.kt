package com.sleekydz86.rag.presentation.controller

import com.sleekydz86.rag.infrastructure.external.RedisBasedSSEServer
import com.sleekydz86.rag.infrastructure.external.sse.SSEMsgType
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@RestController
@RequestMapping("/sse")
class SseController(
    private val redisBasedSSEServer: RedisBasedSSEServer
) {

    @GetMapping("/connect")
    fun connect(@RequestParam userId: String): SseEmitter {
        val emitter = SseEmitter(Long.MAX_VALUE)

        emitter.onCompletion {
            redisBasedSSEServer.close(userId)
        }
        emitter.onTimeout {
            redisBasedSSEServer.close(userId)
        }
        emitter.onError {
            redisBasedSSEServer.close(userId)
        }

        redisBasedSSEServer.addConnection(userId, emitter)

        return emitter
    }

    @GetMapping("/stats")
    fun getStats(): Map<String, Any> {
        return mapOf(
            "connectionCount" to redisBasedSSEServer.getConnectionCount(),
            "activeUsers" to redisBasedSSEServer.getActiveUsers()
        )
    }

    @PostMapping("/broadcast")
    fun broadcastMessage(@RequestParam message: String, @RequestParam type: String = "add") {
        redisBasedSSEServer.getActiveUsers().forEach { userId ->
            redisBasedSSEServer.sendMsg(userId, message, SSEMsgType.valueOf(type.uppercase()))
        }
    }
}
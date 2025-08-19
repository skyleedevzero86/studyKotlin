package com.sleekydz86.rag.infrastructure.external

import com.sleekydz86.rag.infrastructure.external.sse.SSEMsgType
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

object SSEServer {
    private val connections = mutableMapOf<String, SseEmitter>()

    fun sendMsg(userId: String, message: String, type: SSEMsgType) {
        connections[userId]?.let { emitter ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name(type.name.lowercase())
                        .data(message)
                )
            } catch (e: Exception) {
                close(userId)
            }
        }
    }

    fun close(userId: String) {
        connections.remove(userId)?.complete()
    }

    fun addConnection(userId: String, emitter: SseEmitter) {
        connections[userId] = emitter
    }
}
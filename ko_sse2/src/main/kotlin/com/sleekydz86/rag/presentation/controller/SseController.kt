package com.sleekydz86.rag.presentation.controller

import com.sleekydz86.rag.infrastructure.external.SSEServer
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/sse")
class SseController {

    @GetMapping("/connect")
    fun connect(@RequestParam userId: String): SseEmitter {
        val emitter = SseEmitter(Long.MAX_VALUE)

        emitter.onCompletion { SSEServer.close(userId) }
        emitter.onTimeout { SSEServer.close(userId) }
        emitter.onError { SSEServer.close(userId) }

        SSEServer.addConnection(userId, emitter)

        return emitter
    }
}
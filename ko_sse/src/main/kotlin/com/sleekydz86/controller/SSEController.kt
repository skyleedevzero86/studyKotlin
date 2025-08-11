package com.sleekydz86.controller

import com.sleekydz86.utils.SSEServer
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/sse")
class SSEController {
    @GetMapping(
        path = ["/connect"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun connect(@RequestParam userId: String): SseEmitter {
        return SSEServer.connect(userId)
    }
}
package com.sleekydz86.global.config

import com.sleekydz86.videocall.service.WebRTCSignalingService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

@Configuration
class WebSocketConfig(
    private val signalingService: WebRTCSignalingService
) {

    @Bean
    fun handlerMapping(): HandlerMapping {
        val map = hashMapOf<String, WebSocketHandler>()
        map["/ws/signaling"] = WebSocketHandler { session: WebSocketSession ->
            signalingService.handleWebSocketConnection(session)
        }

        return SimpleUrlHandlerMapping().apply {
            urlMap = map
            order = -1
        }
    }

    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter()
    }
}
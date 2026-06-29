package com.kochat.adapter.inbound.websocket

import com.kochat.adapter.inbound.websocket.webmedia.WebMediaWebSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val chatWebSocketHandler: ChatWebSocketHandler,
    private val webMediaWebSocketHandler: WebMediaWebSocketHandler,
    private val webSocketHandshakeInterceptor: WebSocketHandshakeInterceptor,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(chatWebSocketHandler, "/api/v1/ws/chat")
            .addInterceptors(webSocketHandshakeInterceptor)
            .setAllowedOrigins("http://localhost:3000")
        registry.addHandler(webMediaWebSocketHandler, "/api/v1/ws/webmedia")
            .addInterceptors(webSocketHandshakeInterceptor)
            .setAllowedOrigins("http://localhost:3000")
    }
}

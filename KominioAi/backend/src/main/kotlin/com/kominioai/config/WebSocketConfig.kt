package com.kominioai.config

import com.kominioai.global.util.QuizWebSocketHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

@Configuration
class WebSocketConfig {

    @Bean
    fun webSocketHandlerMapping(handler: QuizWebSocketHandler): HandlerMapping {
        val map = mapOf("/ws/quiz/{surveyId}" to handler)
        return SimpleUrlHandlerMapping(map, 1)
    }
}
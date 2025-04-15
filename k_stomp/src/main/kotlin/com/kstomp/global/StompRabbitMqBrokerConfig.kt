package com.kstomp.global

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class StompRabbitMqBrokerConfig : WebSocketMessageBrokerConfigurer {
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/app")

        registry.enableStompBrokerRelay("/exchange", "/queue", "/topic", "/amq/queue")
            .setRelayHost("localhost")
            .setRelayPort(61613)  // This is the STOMP port, not the AMQP port
            .setClientLogin("admin")
            .setClientPasscode("admin")
            .setSystemLogin("admin")
            .setSystemPasscode("admin")
            .setSystemHeartbeatSendInterval(10000)
            .setSystemHeartbeatReceiveInterval(10000)
    }
}
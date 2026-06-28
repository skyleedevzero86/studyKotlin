package com.kochat.support

import com.kochat.global.application.chat.RedisMessageBroker
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.StringRedisSerializer

@TestConfiguration
class ChatTestRedisConfig {
    @Bean
    @Primary
    fun testRedisConnectionFactory(): RedisConnectionFactory =
        LettuceConnectionFactory("localhost", 16379).apply {
            validateConnection = false
        }

    @Bean
    @Primary
    fun testRedisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> =
        RedisTemplate<String, String>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            afterPropertiesSet()
        }

    @Bean
    @Primary
    fun testRedisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory,
    ): RedisMessageListenerContainer =
        RedisMessageListenerContainer().apply {
            setConnectionFactory(connectionFactory)
        }

    @Bean
    @Primary
    fun testRedisMessageBroker(
        redisTemplate: RedisTemplate<String, String>,
        messageListenerContainer: RedisMessageListenerContainer,
        objectMapper: com.fasterxml.jackson.databind.ObjectMapper,
    ): RedisMessageBroker = RedisMessageBroker(redisTemplate, messageListenerContainer, objectMapper)
}

package com.azure.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericToStringSerializer

@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}") private val redisHost: String,
    @Value("\${spring.data.redis.port}") private val redisPort: Int,
    @Value("\${spring.data.redis.password}") private val password: String,
    @Value("\${spring.data.redis.database}") private val database: Int
) {

    @Bean
    fun lettuceConnectionFactory() = LettuceConnectionFactory(
        RedisStandaloneConfiguration(redisHost, redisPort).apply {
            setPassword(password)
            setDatabase(database)
        }
    ).apply {
        afterPropertiesSet()
    }

    @Bean
    fun redisTemplate(connectionFactory: LettuceConnectionFactory) = RedisTemplate<String, Any>().apply {
        setConnectionFactory(connectionFactory)
        setDefaultSerializer(GenericToStringSerializer(Any::class.java))
    }
}
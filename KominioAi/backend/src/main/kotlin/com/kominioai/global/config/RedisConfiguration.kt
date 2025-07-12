package com.kominioai.global.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
@ConditionalOnProperty(name = ["spring.data.redis.host"]) // Redis 호스트가 설정된 경우에만 활성화
class RedisConfiguration {

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerModule(JavaTimeModule())
            .activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
            )
    }

    @Bean
    fun reactiveRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory,
        objectMapper: ObjectMapper
    ): ReactiveRedisTemplate<String, Any> {
        val jsonSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        val stringSerializer = StringRedisSerializer()

        val serializationContext = RedisSerializationContext.newSerializationContext<String, Any>()
            .key(stringSerializer)
            .value(jsonSerializer)
            .hashKey(stringSerializer)
            .hashValue(jsonSerializer)
            .build()

        return ReactiveRedisTemplate(connectionFactory, serializationContext)
    }

    companion object {
        // 캐시 키 상수
        const val SURVEY_CACHE_PREFIX = "survey:"
        const val SURVEY_WITH_QUESTIONS_CACHE_PREFIX = "survey:with-questions:"
        const val PUBLISHED_SURVEYS_CACHE_KEY = "surveys:published"
        const val SURVEY_STATISTICS_CACHE_PREFIX = "survey:statistics:"

        // 캐시 만료 시간
        val SURVEY_CACHE_TTL = Duration.ofMinutes(30)
        val SURVEY_WITH_QUESTIONS_CACHE_TTL = Duration.ofMinutes(15)
        val PUBLISHED_SURVEYS_CACHE_TTL = Duration.ofMinutes(10)
        val SURVEY_STATISTICS_CACHE_TTL = Duration.ofMinutes(5)
    }
}
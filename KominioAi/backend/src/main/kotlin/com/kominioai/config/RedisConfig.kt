package com.kominioai.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    @Primary
    fun reactiveStringRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveStringRedisTemplate {
        return ReactiveStringRedisTemplate(factory)
    }

    @Bean
    fun reactiveRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> {
        val keySerializer = StringRedisSerializer()
        val valueSerializer = StringRedisSerializer()

        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, String>()
            .key(keySerializer)
            .value(valueSerializer)
            .hashKey(keySerializer)
            .hashValue(valueSerializer)
            .build()

        return ReactiveRedisTemplate(factory, serializationContext)
    }

    @Bean
    fun reactiveObjectRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Any> {
        val keySerializer = StringRedisSerializer()

        val objectMapper = ObjectMapper().apply {
            registerModule(JavaTimeModule())
            activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
            )
        }

        val valueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)

        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, Any>()
            .key(keySerializer)
            .value(valueSerializer)
            .hashKey(keySerializer)
            .hashValue(valueSerializer)
            .build()

        return ReactiveRedisTemplate(factory, serializationContext)
    }

    @Bean
    fun reactiveSurveyCacheRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, com.kominioai.domain.survey.adapter.out.cache.SurveyCacheEntity> {
        val keySerializer = StringRedisSerializer()

        val objectMapper = ObjectMapper().apply {
            registerModule(JavaTimeModule())
        }

        val valueSerializer = Jackson2JsonRedisSerializer(objectMapper, com.kominioai.domain.survey.adapter.out.cache.SurveyCacheEntity::class.java)

        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, com.kominioai.domain.survey.adapter.out.cache.SurveyCacheEntity>()
            .key(keySerializer)
            .value(valueSerializer)
            .hashKey(keySerializer)
            .hashValue(valueSerializer)
            .build()

        return ReactiveRedisTemplate(factory, serializationContext)
    }

    @Bean
    fun reactiveQuizParticipationCacheRedisTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, com.kominioai.domain.survey.adapter.out.cache.QuizParticipationCacheEntity> {
        val keySerializer = StringRedisSerializer()

        val objectMapper = ObjectMapper().apply {
            registerModule(JavaTimeModule())
        }

        val valueSerializer = Jackson2JsonRedisSerializer(objectMapper, com.kominioai.domain.survey.adapter.out.cache.QuizParticipationCacheEntity::class.java)

        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, com.kominioai.domain.survey.adapter.out.cache.QuizParticipationCacheEntity>()
            .key(keySerializer)
            .value(valueSerializer)
            .hashKey(keySerializer)
            .hashValue(valueSerializer)
            .build()

        return ReactiveRedisTemplate(factory, serializationContext)
    }
}
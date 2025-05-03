package com.komroonga.global.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

/**
 * Redis 설정 클래스
 * 캐시 및 데이터 직렬화를 위한 설정 제공
 */
@Configuration
@EnableCaching
class RedisConfig {

    /**
     * Jackson ObjectMapper 설정
     * Kotlin 모듈 및 타입 정보 처리 최적화
     */
    @Bean(name = ["redisObjectMapper"])
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerKotlinModule()
            // 타입 정보 포함, 클래스별 명시적 처리
            enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT)
        }
    }

    /**
     * RedisTemplate 설정
     * 키는 문자열, 값은 JSON 직렬화
     */
    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory, objectMapper: ObjectMapper): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            setConnectionFactory(redisConnectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
            afterPropertiesSet()
        }
    }

    /**
     * LettuceConnectionFactory 설정
     * Redis 연결 설정
     */
    @Bean
    fun lettuceConnectionFactory(): LettuceConnectionFactory {
        val redisStandaloneConfiguration = RedisStandaloneConfiguration("localhost", 6379)
        val clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofMillis(500))
            .shutdownTimeout(Duration.ZERO)
            .build()
        return LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig)
    }

    /**
     * RedisCacheManager 설정
     * 캐시 설정 및 TTL 정의
     */
    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory, objectMapper: ObjectMapper): RedisCacheManager {
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer(objectMapper)
                )
            )

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withCacheConfiguration(
                "posts",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(5))
                    .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                            GenericJackson2JsonRedisSerializer(objectMapper)
                        )
                    )
            )
            .withCacheConfiguration(
                "members",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(30))
                    .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                            GenericJackson2JsonRedisSerializer(objectMapper)
                        )
                    )
            )
            .build()
    }
}
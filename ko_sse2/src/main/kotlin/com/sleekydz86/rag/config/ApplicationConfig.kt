package com.sleekydz86.rag.config

import com.sleekydz86.rag.presentation.controller.SseController
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.document.Document
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.util.Optional

@Configuration
class ApplicationConfig {

    @Bean
    fun sseController(): SseController = SseController()

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory

        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()

        template.valueSerializer = GenericJackson2JsonRedisSerializer()
        template.hashValueSerializer = GenericJackson2JsonRedisSerializer()

        template.afterPropertiesSet()
        return template
    }

    @Bean
    @ConditionalOnMissingBean
    fun vectorStore(): VectorStore = object : VectorStore {
        override fun add(documents: List<Document>) {
            // 실제 구현에서는 Redis에 문서를 저장
        }

        override fun similaritySearch(searchRequest: SearchRequest): List<Document> {
            // 실제 구현에서는 Redis에서 유사도 검색
            return emptyList()
        }

        override fun similaritySearch(query: String): List<Document> {
            // 실제 구현에서는 Redis에서 유사도 검색
            return emptyList()
        }

        override fun delete(idList: List<String>): Optional<Boolean> {
            // 실제 구현에서는 Redis에서 문서 삭제
            return Optional.of(true)
        }
    }
}
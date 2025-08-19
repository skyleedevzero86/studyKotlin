package com.sleekydz86.rag.infrastructure.cache

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisCacheStrategy<K, V>(
    private val redisTemplate: RedisTemplate<String, Any>
) : CacheStrategy<K, V> {

    @Suppress("UNCHECKED_CAST")
    override fun get(key: K): V? =
        redisTemplate.opsForValue().get(key.toString()) as? V

    override fun put(key: K, value: V) {
        redisTemplate.opsForValue().set(key.toString(), value as Any, Duration.ofHours(1))
    }

    override fun remove(key: K) {
        redisTemplate.delete(key.toString())
    }

    override fun clear() {
        redisTemplate.connectionFactory?.connection?.flushAll()
    }
}
package com.kominioai.domain.auth.adapter.out.cache

import com.kominioai.domain.auth.application.port.out.CachePort
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class RedisCacheAdapter(
    private val redisTemplate: ReactiveStringRedisTemplate
) : CachePort {
    override fun set(key: String, value: String, ttlSeconds: Long): Mono<Boolean> =
        redisTemplate.opsForValue().set(key, value).then(redisTemplate.expire(key, java.time.Duration.ofSeconds(ttlSeconds)))

    override fun get(key: String): Mono<String?> =
        redisTemplate.opsForValue().get(key)

    override fun delete(key: String): Mono<Boolean> =
        redisTemplate.delete(key).map { it > 0 }

    override fun exists(key: String): Mono<Boolean> =
        redisTemplate.hasKey(key)

    override fun increment(key: String): Mono<Long> =
        redisTemplate.opsForValue().increment(key)

    override fun increment(key: String, delta: Long): Mono<Long> =
        redisTemplate.opsForValue().increment(key, delta)

    override fun expire(key: String, ttlSeconds: Long): Mono<Boolean> =
        redisTemplate.expire(key, java.time.Duration.ofSeconds(ttlSeconds))
}
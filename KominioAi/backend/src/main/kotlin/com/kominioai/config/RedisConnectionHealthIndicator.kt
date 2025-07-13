package com.kominioai.config

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisConnectionHealthIndicator(
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) : HealthIndicator {

    override fun health(): Health {
        return try {
            val testKey = "health-check"
            val testValue = "OK"

            val result = redisTemplate.opsForValue()
                .set(testKey, testValue, Duration.ofSeconds(1))
                .then(redisTemplate.opsForValue().get(testKey))
                .timeout(Duration.ofSeconds(5))
                .block()

            if (result == testValue) {
                Health.up()
                    .withDetail("message", "Redis 연결 성공")
                    .withDetail("server", "localhost:6379")
                    .build()
            } else {
                Health.down()
                    .withDetail("message", "Redis 응답 불일치")
                    .build()
            }
        } catch (e: Exception) {
            Health.down()
                .withDetail("message", "Redis 연결 실패: ${e.message}")
                .withException(e)
                .build()
        }
    }
}
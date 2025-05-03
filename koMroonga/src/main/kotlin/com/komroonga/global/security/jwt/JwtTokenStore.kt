package com.komroonga.global.security.jwt

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * JWT 토큰 저장소
 * Redis를 사용하여 토큰 관리 (중복 로그인 방지)
 */
@Component
class JwtTokenStore(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val tokenPrefix = "jwt:token:"

    /**
     * 사용자 이름과 토큰을 Redis에 저장
     */
    fun saveToken(username: String, token: String, expirationMs: Long) {
        try {
            redisTemplate.opsForValue().set(
                "$tokenPrefix$username",
                token,
                expirationMs,
                TimeUnit.MILLISECONDS
            )
            logger.info("Token saved for user: {}", username)
        } catch (e: Exception) {
            logger.error("Failed to save token for user: {}, error: {}", username, e.message, e)
            throw RuntimeException("Redis token save failed", e)
        }
    }

    /**
     * 토큰 무효화
     */
    fun invalidateToken(token: String) {
        try {
            val keys = redisTemplate.keys("$tokenPrefix*")
            keys?.forEach { key ->
                val storedToken = redisTemplate.opsForValue().get(key) as? String
                if (storedToken == token) {
                    redisTemplate.delete(key)
                    logger.info("Token invalidated: {}", key)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to invalidate token, error: {}", e.message, e)
        }
    }
}
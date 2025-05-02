package com.komroonga.global.security.jwt

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.concurrent.TimeUnit

@Service
class JwtTokenStore(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private val tokenPrefix = "jwt:token:"
    private val blacklistPrefix = "jwt:blacklist:"

    /**
     * 토큰을 Redis에 저장합니다. 이미 동일 사용자의 토큰이 있다면 기존 토큰을 무효화합니다.
     */
    fun saveToken(username: String, token: String, expirationTime: Long) {
        // 기존 토큰이 있는지 확인
        val existingToken = getTokenByUsername(username)
        
        // 기존 토큰이 있다면 블랙리스트에 추가
        if (existingToken != null) {
            invalidateToken(existingToken)
        }
        
        // 새 토큰 저장
        redisTemplate.opsForValue().set("$tokenPrefix$username", token)
        redisTemplate.expire("$tokenPrefix$username", expirationTime, TimeUnit.MILLISECONDS)
        
        // 토큰으로 사용자 조회 가능하도록 역방향 매핑 저장
        redisTemplate.opsForValue().set("$tokenPrefix$token", username)
        redisTemplate.expire("$tokenPrefix$token", expirationTime, TimeUnit.MILLISECONDS)
    }

    /**
     * 사용자명으로 토큰을 조회합니다.
     */
    fun getTokenByUsername(username: String): String? {
        return redisTemplate.opsForValue().get("$tokenPrefix$username") as? String
    }

    /**
     * 토큰으로 사용자명을 조회합니다.
     */
    fun getUsernameByToken(token: String): String? {
        return redisTemplate.opsForValue().get("$tokenPrefix$token") as? String
    }

    /**
     * 토큰을 무효화합니다 (로그아웃 시 사용).
     */
    fun invalidateToken(token: String) {
        val username = getUsernameByToken(token)
        
        // 토큰을 블랙리스트에 추가
        if (username != null) {
            // 토큰의 남은 유효 시간 계산
            val ttl = redisTemplate.getExpire("$tokenPrefix$token", TimeUnit.MILLISECONDS)
            if (ttl > 0) {
                // 블랙리스트에 추가 (원래 만료 시간까지만 유지)
                redisTemplate.opsForValue().set("$blacklistPrefix$token", "INVALIDATED")
                redisTemplate.expire("$blacklistPrefix$token", ttl, TimeUnit.MILLISECONDS)
            }
            
            // 사용자 토큰 매핑 삭제
            redisTemplate.delete("$tokenPrefix$username")
        }
        
        // 토큰 -> 사용자 매핑 삭제
        redisTemplate.delete("$tokenPrefix$token")
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인합니다 (무효화된 토큰인지 확인).
     */
    fun isTokenBlacklisted(token: String): Boolean {
        return redisTemplate.hasKey("$blacklistPrefix$token")
    }
}
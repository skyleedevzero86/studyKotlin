package com.sleekydz86.youtubemsa.download.persistence

import com.sleekydz86.youtubemsa.download.ledger.ClaimLock
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisClaimLock(
    private val redis: StringRedisTemplate,
) : ClaimLock {
    override fun <T> hold(id: Long, action: () -> T): T? {
        val key = "youtubemsa:download:$id"
        val acquired = redis.opsForValue().setIfAbsent(key, "1", Duration.ofMinutes(15)) == true
        if (!acquired) return null
        try {
            return action()
        } finally {
            redis.delete(key)
        }
    }
}

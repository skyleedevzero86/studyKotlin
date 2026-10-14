package com.sleekydz86.youtubedownload.global.redis

import com.sleekydz86.youtubedownload.global.cdc.DownloadLock
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisDownloadLock(
    private val redis: StringRedisTemplate,
) : DownloadLock {
    override fun hold(id: Long, action: () -> Unit) {
        val key = "youtube:download:$id"
        val acquired = redis.opsForValue().setIfAbsent(key, "1", Duration.ofMinutes(15)) == true
        if (!acquired) return
        try {
            action()
        } finally {
            redis.delete(key)
        }
    }
}

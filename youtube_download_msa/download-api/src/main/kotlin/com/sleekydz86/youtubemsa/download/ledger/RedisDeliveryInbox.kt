package com.sleekydz86.youtubemsa.download.ledger

import com.sleekydz86.youtubemsa.shared.DeliveryInbox
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisDeliveryInbox(
    private val redis: StringRedisTemplate,
) : DeliveryInbox {
    override fun alreadyDone(consumer: String, eventId: String): Boolean = redis.hasKey(key(consumer, eventId))

    override fun markDone(consumer: String, eventId: String) {
        redis.opsForValue().set(key(consumer, eventId), "1", Duration.ofDays(7))
    }

    private fun key(consumer: String, eventId: String) = "youtubemsa:inbox:$consumer:$eventId"
}

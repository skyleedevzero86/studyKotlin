package com.sleekydz86.rag.infrastructure.external

import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener

interface RedisMessageListener : MessageListener {
    override fun onMessage(message: Message, pattern: ByteArray?)
}
package com.kominioai.config

import org.slf4j.LoggerFactory


class RedisMessageHandler {
    private val logger = LoggerFactory.getLogger(RedisMessageHandler::class.java)

    fun handleMessage(message: String) {
        logger.info("Redis 메시지 수신: $message")
    }
}
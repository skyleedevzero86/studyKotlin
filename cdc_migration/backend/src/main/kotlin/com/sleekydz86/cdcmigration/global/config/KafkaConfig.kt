package com.sleekydz86.cdcmigration.global.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaConfig {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun kafkaErrorHandler(): DefaultErrorHandler {
        val handler = DefaultErrorHandler({ record, exception ->
            log.error("CDC 이벤트를 처리하지 못해 건너뜁니다. 토픽={} 파티션={} 오프셋={} 원인={}",
                record.topic(), record.partition(), record.offset(), exception.message)
        }, FixedBackOff(2000L, 10L))
        handler.setAckAfterHandle(false)
        return handler
    }
}

package com.kochat.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.kafka")
data class KafkaProperties(
    val enabled: Boolean = false,
    val bootstrapServers: String = "localhost:9092",
    val topics: KafkaTopicProperties = KafkaTopicProperties(),
    val consumerGroups: KafkaConsumerGroupProperties = KafkaConsumerGroupProperties(),
    val outboxRelayIntervalMs: Long = 3000,
    val outboxBatchSize: Int = 50,
    val outboxMaxRetries: Int = 10,
    val consumerRetryAttempts: Int = 4,
    val consumerRetryDelayMs: Long = 1000,
    val lagWarningThreshold: Long = 100,
    val lagCriticalThreshold: Long = 1000,
    val lagMonitorIntervalMs: Long = 30000,
)

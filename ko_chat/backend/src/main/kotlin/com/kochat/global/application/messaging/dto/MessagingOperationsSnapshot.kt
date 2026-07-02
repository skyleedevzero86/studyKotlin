package com.kochat.global.application.messaging.dto

import java.time.LocalDateTime

data class MessagingOperationsSnapshot(
    val outbox: OutboxStatusSnapshot,
    val processedEvents: ProcessedEventSnapshot,
    val dlq: DlqStatusSnapshot,
    val consumerLag: List<ConsumerLagSnapshot>,
    val lagHealthy: Boolean,
    val checkedAt: LocalDateTime,
)

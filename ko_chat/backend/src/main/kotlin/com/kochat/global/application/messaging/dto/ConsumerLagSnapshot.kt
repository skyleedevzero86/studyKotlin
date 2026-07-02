package com.kochat.global.application.messaging.dto

data class ConsumerLagSnapshot(
    val consumerGroup: String,
    val topic: String,
    val partition: Int,
    val currentOffset: Long,
    val endOffset: Long,
    val lag: Long,
)

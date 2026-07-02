package com.kochat.global.application.messaging.dto

data class ProcessedEventSnapshot(
    val total: Long,
    val byConsumer: Map<String, Long>,
)

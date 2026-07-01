package com.kochat.domain.messaging.model

enum class OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED,
}

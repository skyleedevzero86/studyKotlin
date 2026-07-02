package com.kochat.global.application.messaging.dto

data class OutboxStatusSnapshot(
    val pending: Long,
    val published: Long,
    val failed: Long,
)

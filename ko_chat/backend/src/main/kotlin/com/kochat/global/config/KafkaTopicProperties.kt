package com.kochat.global.config

data class KafkaTopicProperties(
    val messageEvents: String = "chat.message.events",
    val messageEventsRetry: String = "chat.message.events.retry",
    val attachmentEvents: String = "chat.attachment.events",
    val attachmentEventsRetry: String = "chat.attachment.events.retry",
    val messageEventsDlq: String = "chat.message.events.dlq",
    val attachmentEventsDlq: String = "chat.attachment.events.dlq",
    val outboxDlq: String = "chat.outbox.events.dlq",
)

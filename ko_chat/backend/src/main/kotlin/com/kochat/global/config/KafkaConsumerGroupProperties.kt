package com.kochat.global.config

data class KafkaConsumerGroupProperties(
    val audit: String = "ko-chat-audit",
    val searchIndex: String = "ko-chat-search-index",
    val attachment: String = "ko-chat-attachment",
    val milvus: String = "ko-chat-milvus",
    val dlq: String = "ko-chat-dlq",
)

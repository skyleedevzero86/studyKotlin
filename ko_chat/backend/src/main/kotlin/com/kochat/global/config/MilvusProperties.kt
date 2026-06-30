package com.kochat.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.milvus")
data class MilvusProperties(
    val enabled: Boolean = true,
    val host: String = "localhost",
    val port: Int = 19530,
    val collection: String = "chat_attachments",
    val vectorDim: Int = 8,
)

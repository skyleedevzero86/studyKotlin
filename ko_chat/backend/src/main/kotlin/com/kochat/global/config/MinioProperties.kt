package com.kochat.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.minio")
data class MinioProperties(
    val enabled: Boolean = true,
    val endpoint: String = "http://localhost:9000",
    val accessKey: String = "minioadmin",
    val secretKey: String = "minioadmin",
    val bucket: String = "ko-chat",
    val presignExpirySeconds: Long = 604800,
)

package com.sleekydz86.oauth.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.encryption")
data class EncryptionProperties(
    val secret: String,
)

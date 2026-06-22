package com.kochat.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpireTime: Long,
    val signingAlgorithm: String = "HS256",
)

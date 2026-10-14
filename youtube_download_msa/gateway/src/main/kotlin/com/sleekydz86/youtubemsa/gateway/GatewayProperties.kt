package com.sleekydz86.youtubemsa.gateway

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "study.gateway")
data class GatewayProperties(
    val downloadApi: String,
    val searchApi: String,
)

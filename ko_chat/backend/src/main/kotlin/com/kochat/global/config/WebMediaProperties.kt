package com.kochat.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.webmedia")
data class WebMediaProperties(
    val apiUrl: String = "http://localhost:1985",
    val streamUrl: String = "webrtc://localhost",
)

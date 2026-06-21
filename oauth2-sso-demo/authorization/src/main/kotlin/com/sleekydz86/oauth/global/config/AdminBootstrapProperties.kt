package com.sleekydz86.oauth.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.admin.bootstrap")
data class AdminBootstrapProperties(
    val username: String = "admin",
    val password: String = "admin1234!@#",
    val enabled: Boolean = true,
)

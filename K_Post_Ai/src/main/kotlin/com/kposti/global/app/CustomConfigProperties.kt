package com.kposti.global.app

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "custom")
class CustomConfigProperties {
    data class NotProdMember(
        val username: String,
        val nickname: String,
        val profileImgUrl: String
    ) {
        fun apiKey() = username
    }

    lateinit var notProdMembers: List<NotProdMember>
}

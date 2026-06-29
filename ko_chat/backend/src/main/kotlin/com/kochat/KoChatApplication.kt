package com.kochat

import com.kochat.global.config.AdminBootstrapProperties
import com.kochat.global.config.EncryptionProperties
import com.kochat.global.config.JwtProperties
import com.kochat.global.config.WebMediaProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableConfigurationProperties(JwtProperties::class, AdminBootstrapProperties::class, EncryptionProperties::class, WebMediaProperties::class)
class KoChatApplication

fun main(args: Array<String>) {
    runApplication<KoChatApplication>(*args)
}

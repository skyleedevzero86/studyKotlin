package com.kochat

import com.kochat.global.config.AdminBootstrapProperties
import com.kochat.global.config.EncryptionProperties
import com.kochat.global.config.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class, AdminBootstrapProperties::class, EncryptionProperties::class)
class KoChatApplication

fun main(args: Array<String>) {
    runApplication<KoChatApplication>(*args)
}

package com.sleekydz86.oauth

import com.sleekydz86.oauth.global.config.AdminBootstrapProperties
import com.sleekydz86.oauth.global.config.EncryptionProperties
import com.sleekydz86.oauth.global.config.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class, AdminBootstrapProperties::class, EncryptionProperties::class)
class Oauth2SsoDemoApplication

fun main(args: Array<String>) {
    runApplication<Oauth2SsoDemoApplication>(*args)
}

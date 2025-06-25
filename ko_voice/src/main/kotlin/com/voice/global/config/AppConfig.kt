package com.voice.global.config

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
class AppConfig {

    @Bean
    fun httpClient(): CloseableHttpClient {
        return HttpClients.createDefault()
    }

    @Bean
    fun uploadDir(): File {
        val uploadDir = File("./uploads")
        if (!uploadDir.exists()) {
            uploadDir.mkdirs()
        }
        return uploadDir
    }
}
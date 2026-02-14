package com.sleekydz86.komongo2.global.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MongoConfigLogger {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${spring.data.mongodb.uri:}")
    private var mongoUri: String = ""

    @Value("\${spring.data.mongodb.host:}")
    private var mongoHost: String = ""

    @Value("\${spring.data.mongodb.port:0}")
    private var mongoPort: Int = 0

    @Bean
    fun logMongoConfig(): ApplicationRunner {
        return ApplicationRunner {
            val masked = if (mongoUri.isNotBlank()) {
                mongoUri.replace(Regex("://([^:]+):([^@]+)@"), "://***:***@")
            } else {
                "(uri 없음 - host=$mongoHost, port=$mongoPort)"
            }
            log.info("MongoDB 연결 설정: {}", masked)
        }
    }
}

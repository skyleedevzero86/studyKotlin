package com.sleekydz86.komongo2.global.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class MongoClientConfig {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${spring.data.mongodb.uri}")
    private lateinit var uri: String

    @Primary
    @Bean
    fun mongoClient(): MongoClient {
        log.info("MongoDB MongoClient 생성 (URI 사용, 인증 포함)")
        return MongoClients.create(uri)
    }
}

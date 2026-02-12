package com.sleekydz86.komongo1.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MongoConfig {

    @Bean
    fun mongoClient(
        @Value("\${spring.data.mongodb.uri:mongodb://admin:admin123@localhost:27017/soso?authSource=admin}") uri: String
    ): MongoClient {
        return MongoClients.create(uri)
    }
}

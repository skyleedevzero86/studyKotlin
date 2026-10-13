package com.sleekydz86.densevector.global.config

import com.sleekydz86.densevector.global.embedding.HashingEmbedder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EmbeddingConfig {
    @Bean
    fun hashingEmbedder(properties: VectorProperties): HashingEmbedder = HashingEmbedder(properties.dims)
}

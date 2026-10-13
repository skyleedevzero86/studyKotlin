package com.sleekydz86.densevector2.global.config

import com.sleekydz86.densevector2.global.embedding.HashingEmbedder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EmbeddingConfig {
    @Bean
    fun hashingEmbedder(properties: VectorProperties): HashingEmbedder = HashingEmbedder(properties.dims)
}

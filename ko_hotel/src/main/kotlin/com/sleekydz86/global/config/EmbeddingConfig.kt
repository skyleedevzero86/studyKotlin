package com.sleekydz86.global.config

import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class EmbeddingConfig {

    @Bean(name = ["customEmbeddingModel"])
    @Primary
    fun embeddingModel(@Qualifier("embeddingModel") transformersEmbeddingModel: EmbeddingModel): EmbeddingModel {
        return transformersEmbeddingModel
    }
}
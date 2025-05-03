package com.ragstudy.global.config

import com.ragstudy.domain.entity.EmbeddingModel
import com.ragstudy.domain.entity.SimpleEmbeddingModel
import org.springframework.ai.transformers.TransformersEmbeddingModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EmbeddingConfig {

    @Bean
    fun transformersEmbeddingModel(): TransformersEmbeddingModel {
        return TransformersEmbeddingModel()
    }

    @Bean
    fun embeddingModel(transformersEmbeddingModel: TransformersEmbeddingModel): EmbeddingModel {
        return SimpleEmbeddingModel(transformersEmbeddingModel)
    }
}
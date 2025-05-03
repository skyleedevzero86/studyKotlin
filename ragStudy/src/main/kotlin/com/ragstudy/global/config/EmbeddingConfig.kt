package com.ragstudy.global.config

import com.ragstudy.domain.entity.EmbeddingModel
import com.ragstudy.domain.entity.SimpleEmbeddingModel
import org.springframework.ai.transformers.TransformersEmbeddingModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EmbeddingConfig(
    @Value("\${spring.ai.embedding.transformer.onnx.modelUri}") val modelUri: String,
    @Value("\${spring.ai.embedding.transformer.tokenizer.uri}") val tokenizerUri: String,
    @Value("\${spring.ai.embedding.transformer.onnx.modelOutputName}") val modelOutputName: String
) {

    @Bean
    fun transformersEmbeddingModel(): TransformersEmbeddingModel {
        val options = TransformersEmbeddingModel.Options.builder()
            .withModelUri(modelUri)
            .withTokenizerUri(tokenizerUri)
            .withModelOutputName(modelOutputName)
            .build()

        return TransformersEmbeddingModel(options)
    }

    @Bean
    fun embeddingModel(transformersEmbeddingModel: TransformersEmbeddingModel): EmbeddingModel {
        return SimpleEmbeddingModel(transformersEmbeddingModel)
    }
}
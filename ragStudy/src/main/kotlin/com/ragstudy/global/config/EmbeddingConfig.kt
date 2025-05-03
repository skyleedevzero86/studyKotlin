package com.ragstudy.global.config

import com.ragstudy.domain.entity.EmbeddingModel
import com.ragstudy.domain.entity.SimpleEmbeddingModel
import io.micrometer.observation.ObservationRegistry
import org.springframework.ai.document.MetadataMode
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
        return TransformersEmbeddingModel(MetadataMode.NONE, ObservationRegistry.NOOP).apply {
            setModelResource(modelUri)
            setTokenizerResource(tokenizerUri)
            setTokenizerOptions(mapOf())
            setModelOutputName("last_hidden_state")
        }
    }

    @Bean
    fun embeddingModel(transformersEmbeddingModel: TransformersEmbeddingModel): EmbeddingModel {
        return SimpleEmbeddingModel(transformersEmbeddingModel)
    }
}
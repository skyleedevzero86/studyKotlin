package com.ragstudy.domain.model

import org.springframework.ai.transformers.TransformersEmbeddingModel
import org.springframework.stereotype.Component

@Component
class SimpleEmbeddingModel(
    private val embeddingModel: TransformersEmbeddingModel
) : EmbeddingModel {
    override fun embed(text: String): FloatArray {
        val embedding = embeddingModel.embed(text)
        return embedding.map { it.toFloat() }.toFloatArray()
    }
}

package com.ragstudy.domain.entity

import org.springframework.ai.transformers.TransformersEmbeddingModel

class RealEmbeddingModel(
    private val embeddingModel: TransformersEmbeddingModel
) : EmbeddingModel {
    override fun embed(text: String): FloatArray {
        val embedding = embeddingModel.embed(text)
        return embedding.map { it.toFloat() }.toFloatArray()
    }
}
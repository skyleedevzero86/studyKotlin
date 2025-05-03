package com.ragstudy.domain.service

import com.ragstudy.domain.model.EmbeddingModel
import org.springframework.stereotype.Service

@Service
class EmbedService(private val embeddingModel: EmbeddingModel) {

    fun embedText(text: String): FloatArray {
        return embeddingModel.embed(text)
    }
}
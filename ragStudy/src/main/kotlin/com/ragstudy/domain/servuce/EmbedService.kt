package com.ragstudy.domain.servuce

import com.ragstudy.domain.entity.EmbeddingModel
import org.springframework.stereotype.Service

@Service
class EmbedService(private val embeddingModel: EmbeddingModel) {

    fun embedText(text: String): FloatArray {
        return embeddingModel.embed(text)
    }
}
package com.ragstudy.domain.service

import org.springframework.stereotype.Service
import com.ragstudy.global.util.ClusteringUtils
import kotlin.math.sqrt

@Service
class TextSimilarityService(private val embedService: EmbedService) {

    fun calculateSimilarity(text1: String, text2: String): Double {
        if (text1.isBlank() || text2.isBlank()) {
            return 0.0
        }

        val embedding1 = embedService.embedText(text1)
        val embedding2 = embedService.embedText(text2)

        if (embedding1.isEmpty() || embedding2.isEmpty()) {
            return 0.0
        }

        return cosineSimilarity(embedding1, embedding2)
    }

    private fun cosineSimilarity(vec1: FloatArray, vec2: FloatArray): Double {
        val dotProduct = vec1.zip(vec2) { a, b -> a * b }.sum()
        val magnitude1 = sqrt(vec1.map { it * it }.sum().toDouble())
        val magnitude2 = sqrt(vec2.map { it * it }.sum().toDouble())

        return if (magnitude1 == 0.0 || magnitude2 == 0.0) 0.0 else dotProduct / (magnitude1 * magnitude2)
    }
}
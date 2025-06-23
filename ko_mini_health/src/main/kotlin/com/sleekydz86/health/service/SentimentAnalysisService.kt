package com.sleekydz86.health.service

import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.stereotype.Service
import kotlin.math.cos
import kotlin.math.sqrt

@Service
class SentimentAnalysisService(
    private val embeddingModel: EmbeddingModel
) {
    private val positiveVector = embeddingModel.embed("기분이 좋아요, 행복한 하루입니다")
    private val neutralVector = embeddingModel.embed("오늘은 평범한 하루였어요")
    private val negativeVector = embeddingModel.embed("너무 스트레스 받고 우울해요")

    fun analyzeSentiment(memo: String?): String {
        if (memo.isNullOrBlank()) return "중립"

        val memoEmbedding = embeddingModel.embed(memo)

        // 코사인 유사도 계산
        val positiveSimilarity = cosineSimilarity(memoEmbedding, positiveVector)
        val neutralSimilarity = cosineSimilarity(memoEmbedding, neutralVector)
        val negativeSimilarity = cosineSimilarity(memoEmbedding, negativeVector)

        return when {
            positiveSimilarity > neutralSimilarity && positiveSimilarity > negativeSimilarity -> "긍정"
            negativeSimilarity > positiveSimilarity && negativeSimilarity > neutralSimilarity -> "부정"
            else -> "중립"
        }
    }

    private fun cosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
        var dotProduct = 0f
        var norm1 = 0f
        var norm2 = 0f
        for (i in vec1.indices) {
            dotProduct += vec1[i] * vec2[i]
            norm1 += vec1[i] * vec1[i]
            norm2 += vec2[i] * vec2[i]
        }
        return if (norm1 == 0f || norm2 == 0f) 0f else dotProduct / (sqrt(norm1) * sqrt(norm2))
    }
}
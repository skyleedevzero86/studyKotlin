package com.ragstudy.domain.service

import com.ragstudy.global.util.TextUtils
import org.springframework.stereotype.Service

@Service
class TextSummarizationService(private val embedService: EmbedService) {

    // 추출 요약 (Extractive Summarization) 구현
    fun summarizeText(text: String, sentenceCount: Int = 3): List<String> {
        if (text.isBlank()) {
            return emptyList()
        }

        // 문장 분리
        val sentences = text.split(Regex("[.!?]+"))
            .filter { it.isNotBlank() }
            .map { it.trim() }

        if (sentences.size <= sentenceCount) {
            return sentences
        }

        // 각 문장의 임베딩 생성
        val embeddings = sentences.map { embedService.embedText(it) }

        // 모든 문장의 평균 임베딩 계산 (문서 중심점)
        val avgEmbedding = if (embeddings.isNotEmpty()) {
            val embSize = embeddings[0].size
            val sumEmbedding = FloatArray(embSize) { 0f }

            embeddings.forEach { embedding ->
                for (i in 0 until embSize) {
                    sumEmbedding[i] = sumEmbedding[i] + embedding[i] // 명시적으로 값 할당
                }
            }

            for (i in 0 until embSize) {
                sumEmbedding[i] = sumEmbedding[i] / embeddings.size // 명시적으로 값 할당
            }

            sumEmbedding
        } else {
            FloatArray(0)
        }

        // 각 문장과 평균 임베딩 간의 유사도 계산
        val sentenceScores = embeddings.mapIndexed { idx, embedding ->
            val similarity = cosineSimilarity(embedding, avgEmbedding)
            sentences[idx] to similarity
        }

        // 점수가 높은 순으로 상위 N개 문장 선택
        return sentenceScores
            .sortedByDescending { it.second }
            .take(sentenceCount)
            .map { it.first }
    }

    private fun cosineSimilarity(vec1: FloatArray, vec2: FloatArray): Double {
        if (vec1.isEmpty() || vec2.isEmpty()) return 0.0

        val dotProduct = vec1.zip(vec2) { a, b -> a * b }.sum()
        val magnitude1 = Math.sqrt(vec1.map { it * it }.sum().toDouble())
        val magnitude2 = Math.sqrt(vec2.map { it * it }.sum().toDouble())

        return if (magnitude1 == 0.0 || magnitude2 == 0.0) 0.0 else dotProduct / (magnitude1 * magnitude2)
    }
}
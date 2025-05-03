package com.ragstudy.global.util

object TextUtils {

    fun normalize(text: String): String {
        if (text.isBlank()) {
            return ""
        }

        // 기본 정규화: 소문자로 변환, 특수문자 제거 (한글, 영문, 숫자, 공백 유지)
        return text.lowercase().replace(Regex("[^a-zA-Z0-9가-힣 ]"), " ")
            .replace(Regex("\\s+"), " ") // 연속된 공백은 하나로
            .trim()
    }

    fun tokenize(text: String): List<String> {
        if (text.isBlank()) {
            return emptyList()
        }

        // 한글/영어 모두 지원하는 토큰화
        return text.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
    }

    // 텍스트 요약 (간단한 추출 요약)
    fun summarize(text: String, sentenceCount: Int = 3): String {
        if (text.isBlank()) {
            return ""
        }

        // 문장 분리
        val sentences = text.split(Regex("[.!?]+"))
            .filter { it.isNotBlank() }
            .map { it.trim() }

        if (sentences.size <= sentenceCount) {
            return text
        }

        // 중요 문장 추출 (간단한 알고리즘)
        val wordFreq = tokenize(text.lowercase())
            .filter { it.length > 1 }
            .groupingBy { it }
            .eachCount()

        // 문장 점수화: 문장에 포함된 단어의 빈도 합계
        val sentenceScores = sentences.map { sentence ->
            val tokens = tokenize(sentence.lowercase())
            val score = tokens.sumOf { wordFreq[it] ?: 0 }
            sentence to score.toDouble() / tokens.size.coerceAtLeast(1)
        }

        // 점수가 높은 순으로 상위 N개 문장 선택
        return sentenceScores
            .sortedByDescending { it.second }
            .take(sentenceCount)
            .map { it.first }
            .joinToString(". ") + "."
    }
}
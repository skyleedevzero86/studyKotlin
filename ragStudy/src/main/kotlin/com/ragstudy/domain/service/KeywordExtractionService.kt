package com.ragstudy.domain.service

import com.ragstudy.global.util.TextUtils
import org.springframework.stereotype.Service

@Service
class KeywordExtractionService {

    // 한국어와 영어 불용어 모두 포함
    private val stopwords = setOf(
        // 한국어 불용어
        "은", "는", "이", "가", "을", "를", "의", "에", "에서", "그리고", "하지만", "또한",
        "및", "이런", "저런", "그런", "어떤", "이것", "저것", "그것", "이렇게", "저렇게", "그렇게",
        // 영어 불용어
        "the", "a", "an", "and", "or", "but", "if", "because", "as", "what", "which",
        "this", "that", "these", "those", "then", "just", "so", "than", "such", "both",
        "through", "about", "for", "is", "are", "was", "were", "be", "been", "being",
        "have", "has", "had", "do", "does", "did", "can", "could", "will", "would",
        "should", "on", "in", "to", "from", "by", "with", "of"
    )

    fun extractKeywords(text: String, limit: Int = 5): List<String> {
        if (text.isBlank()) {
            return emptyList()
        }

        val tokens = TextUtils.tokenize(text)

        val filteredTokens = tokens
            .filter { it.length > 1 } // 한 글자 단어 제외
            .filter { it !in stopwords } // 불용어 제외

        val freqMap = filteredTokens.groupingBy { it }.eachCount()

        return freqMap.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(limit)
    }
}
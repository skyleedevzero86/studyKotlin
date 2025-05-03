package com.ragstudy.domain.servuce

import org.springframework.stereotype.Service

@Service
class KeywordExtractionService {

    private val stopwords = setOf("은", "는", "이", "가", "을", "를", "의", "에", "에서", "그리고", "하지만", "또한")

    fun extractKeywords(text: String): List<String> {
        val words = text.split(" ")
            .map { it.trim() }
            .filter { it.isNotBlank() && it !in stopwords }

        val freqMap = words.groupingBy { it }.eachCount()
        return freqMap.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(5)
    }
}

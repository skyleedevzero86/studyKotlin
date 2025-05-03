package com.ragstudy.domain.service

import org.springframework.stereotype.Service

@Service
class SentimentAnalysisService {

    private val positiveWords = setOf("good", "happy", "great", "awesome", "excellent", "love", "amazing", "like")
    private val negativeWords = setOf("bad", "sad", "terrible", "awful", "hate", "angry", "worse", "dislike")

    fun analyzeSentiment(text: String): String {
        val normalizedText = text.lowercase().replace(Regex("[^a-z ]"), "")
        val words = normalizedText.split("\\s+".toRegex())

        val positiveScore = words.count { it in positiveWords }
        val negativeScore = words.count { it in negativeWords }

        return when {
            positiveScore > negativeScore -> "Positive"
            negativeScore > positiveScore -> "Negative"
            else -> "Neutral"
        }
    }
}

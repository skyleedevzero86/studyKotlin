package com.ragstudy.domain.service

import com.ragstudy.global.util.TextUtils
import org.springframework.stereotype.Service

@Service
class SentimentAnalysisService {

    // 감정 분석용 긍정/부정 단어 사전 (한국어/영어)
    private val positiveWords = setOf(
        // 영어 긍정 단어
        "good", "happy", "great", "awesome", "excellent", "love", "amazing", "like", "best",
        "wonderful", "fantastic", "perfect", "beautiful", "better", "nice", "positive", "joy",
        // 한국어 긍정 단어
        "좋은", "좋다", "훌륭한", "훌륭하다", "멋진", "멋지다", "행복한", "행복하다", "아름다운",
        "아름답다", "최고의", "최고", "사랑", "좋아요", "감사", "기쁜", "기쁘다"
    )

    private val negativeWords = setOf(
        // 영어 부정 단어
        "bad", "sad", "terrible", "awful", "hate", "angry", "worse", "dislike", "horrible",
        "poor", "negative", "failure", "disappointment", "disappointed", "waste", "problem",
        // 한국어 부정 단어
        "나쁜", "나쁘다", "슬픈", "슬프다", "끔찍한", "끔찍하다", "화난", "화나다", "싫은",
        "싫다", "실망", "문제", "불만", "불편한", "불편하다", "최악의", "최악"
    )

    fun analyzeSentiment(text: String): Map<String, Any> {
        if (text.isBlank()) {
            return mapOf(
                "sentiment" to "Neutral",
                "score" to 0.0,
                "details" to mapOf("positive" to 0, "negative" to 0, "neutral" to 1)
            )
        }

        val tokens = TextUtils.tokenize(text.lowercase())

        val positiveScore = tokens.count { token ->
            positiveWords.any { positiveWord -> token.contains(positiveWord) }
        }

        val negativeScore = tokens.count { token ->
            negativeWords.any { negativeWord -> token.contains(negativeWord) }
        }

        val totalScore = tokens.size
        val neutralScore = totalScore - (positiveScore + negativeScore)

        val sentimentScore = if (totalScore > 0) {
            (positiveScore - negativeScore).toDouble() / totalScore
        } else {
            0.0
        }

        val sentiment = when {
            sentimentScore > 0.1 -> "Positive"
            sentimentScore < -0.1 -> "Negative"
            else -> "Neutral"
        }

        return mapOf(
            "sentiment" to sentiment,
            "score" to sentimentScore,
            "details" to mapOf(
                "positive" to positiveScore,
                "negative" to negativeScore,
                "neutral" to neutralScore
            )
        )
    }
}
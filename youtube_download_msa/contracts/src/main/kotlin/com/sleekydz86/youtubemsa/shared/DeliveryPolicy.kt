package com.sleekydz86.youtubemsa.shared

object DeliveryPolicy {
    const val maxAttempts = 5
    const val initialDelayMillis = 1_000L
    const val multiplier = 2.0
    const val maxDelayMillis = 16_000L

    fun delayMillis(attempt: Int): Long {
        requireAttempt(attempt)
        var delay = initialDelayMillis.toDouble()
        repeat(attempt - 1) { delay *= multiplier }
        return minOf(delay.toLong(), maxDelayMillis)
    }

    fun deadLetter(attempt: Int): Boolean {
        requireAttempt(attempt)
        return attempt >= maxAttempts
    }

    private fun requireAttempt(attempt: Int) {
        if (attempt < 1) throw StudyException("재시도 횟수는 1 이상이어야 합니다. 입력값: $attempt")
    }
}

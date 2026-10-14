package com.sleekydz86.youtubemsa.shared

object SearchLimits {
    fun normalizeK(k: Int?): Int {
        val value = k ?: 5
        if (value !in 1..20) throw StudyException("k는 1 이상 20 이하여야 합니다. 입력값: $value")
        return value
    }

    fun numCandidates(k: Int, configured: Int): Int {
        if (configured < 1) throw StudyException("num_candidates는 1 이상이어야 합니다. 입력값: $configured")
        return maxOf(k, configured)
    }
}

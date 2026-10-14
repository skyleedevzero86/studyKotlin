package com.sleekydz86.youtubemsa.shared

@JvmInline
value class VideoId(val value: String) {
    init {
        if (!isValid(value)) {
            throw StudyException("영상 ID 형식이 아닙니다. 입력값: $value")
        }
    }

    companion object {
        private val pattern = Regex("^[A-Za-z0-9_-]{11}$")

        fun isValid(value: String): Boolean = pattern.matches(value)
    }
}

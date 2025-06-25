package com.voice.domain.voicememo.dto

data class SpeechRequest(
    val model: String = "playai-tts",
    val text: String,
    val voice: String = "alloy",
    val responseFormat: String = "wav",
    val speed: Double = 1.0
) {
    fun validate(): SpeechRequest {
        require(text.isNotBlank()) { "텍스트는 비어있을 수 없습니다" }
        require(text.length <= 4096) { "텍스트는 4096자를 초과할 수 없습니다" }
        require(speed in 0.25..4.0) { "속도는 0.25와 4.0 사이여야 합니다" }
        require(voice.isNotBlank()) { "음성을 선택해야 합니다" }

        return this
    }
}
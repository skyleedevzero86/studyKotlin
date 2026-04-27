package com.voice.domain.voicememo.dto

import com.voice.domain.speech.TtsOptions

data class SpeechRequest(
    val model: String,
    val text: String,
    val voice: String,
    val responseFormat: String = TtsOptions.RESPONSE_FORMAT,
    val speed: Double = 1.0
) {
    fun validate(): SpeechRequest {
        require(text.isNotBlank()) { "텍스트는 비어있을 수 없습니다" }
        require(text.length <= TtsOptions.GROQ_MAX_INPUT_CHARS) {
            "TTS는 요청당 최대 ${TtsOptions.GROQ_MAX_INPUT_CHARS}자까지 지원됩니다"
        }
        require(model.isNotBlank()) { "TTS 모델이 설정되어야 합니다" }
        require(voice.isNotBlank()) { "음성을 선택해야 합니다" }
        require(responseFormat == TtsOptions.RESPONSE_FORMAT) { "TTS는 wav 형식만 지원합니다" }
        require(speed in 0.5..5.0) { "속도는 0.5와 5.0 사이여야 합니다" }

        return this
    }
}

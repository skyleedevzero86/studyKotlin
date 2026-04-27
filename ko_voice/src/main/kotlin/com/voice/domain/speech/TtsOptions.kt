package com.voice.domain.speech

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TtsOptions(
    @Value("\${speech.api.tts.models.english}") private val englishModel: String,
    @Value("\${speech.api.tts.models.arabic}") private val arabicModel: String,
    @Value("\${speech.api.tts.default-model:english}") private val defaultModelKey: String,
    @Value("\${speech.api.tts.default-voice:troy}") val defaultVoice: String
) {
    companion object {
        const val ENGLISH_MODEL_KEY = "english"
        const val ARABIC_MODEL_KEY = "arabic"
        const val RESPONSE_FORMAT = "wav"
        const val MAX_INPUT_CHARS = 200

        val englishVoices = listOf("autumn", "diana", "hannah", "austin", "daniel", "troy")
        val arabicVoices = listOf("abdullah", "fahad", "sultan", "lulwa", "noura", "aisha")
    }

    private val modelIdsByKey: Map<String, String>
        get() = mapOf(
            ENGLISH_MODEL_KEY to englishModel,
            ARABIC_MODEL_KEY to arabicModel
        )

    fun resolveModelKey(requestedModel: String?): String {
        val model = requestedModel?.trim().orEmpty()

        return when {
            model.isBlank() -> defaultModelKey()
            model in modelIdsByKey -> model
            model == englishModel -> ENGLISH_MODEL_KEY
            model == arabicModel -> ARABIC_MODEL_KEY
            else -> throw IllegalArgumentException("지원하지 않는 TTS 모델입니다: $model")
        }
    }

    fun modelId(modelKey: String): String {
        return modelIdsByKey[modelKey]
            ?: throw IllegalArgumentException("지원하지 않는 TTS 모델입니다: $modelKey")
    }

    fun voicesForModelKey(modelKey: String): List<String> {
        return when (modelKey) {
            ENGLISH_MODEL_KEY -> englishVoices
            ARABIC_MODEL_KEY -> arabicVoices
            else -> emptyList()
        }
    }

    fun resolveVoice(modelKey: String, requestedVoice: String?): String {
        val supportedVoices = voicesForModelKey(modelKey)
        val voice = requestedVoice?.trim().orEmpty()

        return when {
            voice.isBlank() -> defaultVoiceForModel(modelKey)
            voice in supportedVoices -> voice
            else -> throw IllegalArgumentException(
                "지원하지 않는 TTS 음성입니다: $voice. 지원 음성: ${supportedVoices.joinToString(", ")}"
            )
        }
    }

    private fun defaultModelKey(): String {
        return if (defaultModelKey in modelIdsByKey) defaultModelKey else ENGLISH_MODEL_KEY
    }

    private fun defaultVoiceForModel(modelKey: String): String {
        return when (modelKey) {
            ENGLISH_MODEL_KEY -> if (defaultVoice in englishVoices) defaultVoice else englishVoices.first()
            ARABIC_MODEL_KEY -> arabicVoices.first()
            else -> defaultVoice
        }
    }
}

package com.voice.domain.speech

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TtsOptions(
    @Value("\${speech.api.tts.models.english}") private val englishModel: String,
    @Value("\${speech.api.tts.models.arabic}") private val arabicModel: String,
    @Value("\${speech.api.tts.default-model:english}") private val defaultModelKey: String,
    @Value("\${speech.api.tts.default-voice:troy}") val defaultVoice: String,
    @Value("\${speech.api.tts.default-provider:groq}") private val defaultProvider: String,
    @Value("\${speech.api.tts.openai.default-model:gpt-4o-mini-tts}") private val defaultOpenAiModel: String
) {
    companion object {
        const val PROVIDER_GROQ = "groq"
        const val PROVIDER_OPENAI = "openai"
        const val ENGLISH_MODEL_KEY = "english"
        const val ARABIC_MODEL_KEY = "arabic"
        const val RESPONSE_FORMAT = "wav"
        const val GROQ_MAX_INPUT_CHARS = 200
        const val OPENAI_MAX_INPUT_CHARS = 4096

        val englishVoices = listOf("autumn", "diana", "hannah", "austin", "daniel", "troy")
        val arabicVoices = listOf("abdullah", "fahad", "sultan", "lulwa", "noura", "aisha")
        val openAiVoices = listOf(
            "alloy", "ash", "ballad", "coral", "echo", "fable", "onyx", "nova",
            "sage", "shimmer", "verse", "marin", "cedar"
        )
        val openAiModels = setOf("tts-1", "tts-1-hd", "gpt-4o-mini-tts", "gpt-4o-mini-tts-2025-12-15")
    }

    private val modelIdsByKey: Map<String, String>
        get() = mapOf(
            ENGLISH_MODEL_KEY to englishModel,
            ARABIC_MODEL_KEY to arabicModel
        )

    fun resolveModelKey(requestedModel: String?): String {
        return resolveModelKey(PROVIDER_GROQ, requestedModel)
    }

    fun resolveModelKey(provider: String, requestedModel: String?): String {
        if (provider == PROVIDER_OPENAI) {
            val modelId = requestedModel?.trim().orEmpty()
            if (modelId.isBlank()) return defaultOpenAiModel
            require(modelId in openAiModels) { "지원하지 않는 OpenAI TTS 모델입니다: $modelId" }
            return modelId
        }

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
        return modelId(PROVIDER_GROQ, modelKey)
    }

    fun modelId(provider: String, modelKey: String): String {
        if (provider == PROVIDER_OPENAI) {
            require(modelKey in openAiModels) { "지원하지 않는 OpenAI TTS 모델입니다: $modelKey" }
            return modelKey
        }

        return modelIdsByKey[modelKey]
            ?: throw IllegalArgumentException("지원하지 않는 TTS 모델입니다: $modelKey")
    }

    fun voicesForModelKey(modelKey: String): List<String> {
        return voicesForModelKey(PROVIDER_GROQ, modelKey)
    }

    fun voicesForModelKey(provider: String, modelKey: String): List<String> {
        if (provider == PROVIDER_OPENAI) {
            return openAiVoices
        }

        return when (modelKey) {
            ENGLISH_MODEL_KEY -> englishVoices
            ARABIC_MODEL_KEY -> arabicVoices
            else -> emptyList()
        }
    }

    fun resolveVoice(modelKey: String, requestedVoice: String?): String {
        return resolveVoice(PROVIDER_GROQ, modelKey, requestedVoice)
    }

    fun resolveVoice(provider: String, modelKey: String, requestedVoice: String?): String {
        val supportedVoices = voicesForModelKey(provider, modelKey)
        val voice = requestedVoice?.trim().orEmpty()

        return when {
            voice.isBlank() -> defaultVoiceForModel(provider, modelKey)
            voice in supportedVoices -> voice
            else -> throw IllegalArgumentException(
                "지원하지 않는 TTS 음성입니다: $voice. 지원 음성: ${supportedVoices.joinToString(", ")}"
            )
        }
    }

    fun resolveProvider(requestedProvider: String?): String {
        val provider = requestedProvider?.trim()?.lowercase().orEmpty()
        return when {
            provider.isBlank() -> if (defaultProvider == PROVIDER_OPENAI) PROVIDER_OPENAI else PROVIDER_GROQ
            provider == PROVIDER_GROQ -> PROVIDER_GROQ
            provider == PROVIDER_OPENAI -> PROVIDER_OPENAI
            else -> throw IllegalArgumentException("지원하지 않는 TTS provider입니다: $provider")
        }
    }

    fun maxInputChars(provider: String): Int {
        return if (provider == PROVIDER_OPENAI) OPENAI_MAX_INPUT_CHARS else GROQ_MAX_INPUT_CHARS
    }

    fun supportsInstructions(provider: String, modelId: String): Boolean {
        return provider == PROVIDER_OPENAI && modelId != "tts-1" && modelId != "tts-1-hd"
    }

    private fun defaultModelKey(): String {
        return if (defaultModelKey in modelIdsByKey) defaultModelKey else ENGLISH_MODEL_KEY
    }

    private fun defaultVoiceForModel(provider: String, modelKey: String): String {
        if (provider == PROVIDER_OPENAI) {
            return if (defaultVoice in openAiVoices) defaultVoice else openAiVoices.first()
        }

        return when (modelKey) {
            ENGLISH_MODEL_KEY -> if (defaultVoice in englishVoices) defaultVoice else englishVoices.first()
            ARABIC_MODEL_KEY -> arabicVoices.first()
            else -> defaultVoice
        }
    }
}

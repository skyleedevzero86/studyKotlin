package com.voice.domain.voicememo.dto

import java.io.File

data class TranscriptionRequest(
    val audioFile: File,
    val language: String? = "ko",
    val model: String = "whisper-large-v3-turbo",
    val responseFormat: String = "json",
    val temperature: Double = 0.0
)
package com.voice.domain.voicememo.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TranscriptionSegment(
    val id: Int,
    val text: String,
    val start: Double,
    val end: Double,
    val avgLogprob: Double? = null,
    val compressionRatio: Double? = null,
    val noSpeechProb: Double? = null,
    val temperature: Double? = null
)
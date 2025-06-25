package com.voice.domain.voicememo.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TranscriptionResponse(
    val text: String,
    val language: String? = null,
    val duration: Double? = null,
    val segments: List<TranscriptionSegment>? = null
)
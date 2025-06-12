package com.koimg.example1.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    @JsonProperty("max_tokens") val maxTokens: Int,
    val temperature: Double
)
package com.koimg.example2.dto

import com.koimg.example1.dto.GroqMessage

data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val max_tokens: Int = 1000,
    val temperature: Double = 0.7
)
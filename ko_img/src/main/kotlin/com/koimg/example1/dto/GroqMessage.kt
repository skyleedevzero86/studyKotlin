package com.koimg.example1.dto

data class GroqMessage(
    val role: String,
    val content: List<ContentItem>
)
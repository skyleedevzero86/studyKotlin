package com.azure.domain.dto

data class ChatMessage(
    val role: String,
    val content: String
) {
    interface Role {
        companion object {
            const val USER = "user"
            const val SYSTEM = "system"
            const val ASSISTANT = "assistant"
        }
    }
}

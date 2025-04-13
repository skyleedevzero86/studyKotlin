package com.koroutines.ais

import kotlinx.coroutines.delay

class ClaudeRequester : ChatbotRequester {

    override suspend fun request(question: String): String {
        delay(2000)
        println("claude")
        return "claude"
    }
}
package com.koroutines.ais

import kotlinx.coroutines.delay

class ChatGptRequester : ChatbotRequester {

    override suspend fun request(question: String): String {
        delay(1000)
        println("chatgpt")
        return "chatgpt"
    }
}
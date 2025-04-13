package com.koroutines.ais

interface ChatbotRequester {

    suspend fun request(question: String): String
}

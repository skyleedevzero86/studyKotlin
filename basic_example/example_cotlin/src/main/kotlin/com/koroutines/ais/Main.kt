package com.koroutines.ais

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val startTime = System.currentTimeMillis()

    val chatbots = listOf(
        ChatGptRequester() to "ChatGptRequester",
        ClaudeRequester() to "ClaudeRequester",
        DeepSeekRequester() to "DeepSeekRequester"
    )

    val jobs = chatbots.map { (chatbot, name) ->
        launch(CoroutineName(name)) {
            println("[${Thread.currentThread().name}] ${coroutineContext[CoroutineName]?.name} 시작 - 시간: ${System.currentTimeMillis() - startTime} ms")
            chatbot.request("prompt")
            println("[${Thread.currentThread().name}] ${coroutineContext[CoroutineName]?.name} 완료 - 시간: ${System.currentTimeMillis() - startTime} ms")
        }
    }
    jobs.joinAll()

    val endTime = System.currentTimeMillis()
    println("전체 실행 시간: ${endTime - startTime} ms")
}

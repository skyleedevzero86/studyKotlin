package com.koroutines.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

/**
 * 코루틴이 남았는데 프로세스가 끝나면 코루틴도 끝!
 * - Coroutine Builder는 runBlocking, launch
 * - Suspend fun : suspend, delay()..
 */

// 코루틴 활용
fun main2() = runBlocking {
    repeat(1000000) {
        launch {
            delay(1000)
            println("- ${Thread.currentThread().name}")
        }
    }
}

// 스레드 활용
fun main() {
    repeat(10000) {
        thread {
            Thread.sleep(1000)
            println("- ${Thread.currentThread().name}")
        }
    }
}
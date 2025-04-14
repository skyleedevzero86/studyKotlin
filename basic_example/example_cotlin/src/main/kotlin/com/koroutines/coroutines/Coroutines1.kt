package com.koroutines.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * 메인 실행: main
 * 첫 번째 코루틴: DefaultDispatcher-worker-1
 * 세 번째 코루틴: main
 * 두 번째 코루틴: DefaultDispatcher-worker-2
 */

fun main(): Unit = runBlocking {
    println("메인 실행: ${Thread.currentThread().name}")

    launch(Dispatchers.Default) { // 첫 번째 코루틴 - 디스패처 (CPU 작업)
        println("첫 번째 코루틴: ${Thread.currentThread().name}")

        launch {  // 두 번째 코루틴 - 부모의 디스패처 상속, Default 사용
            delay(1000L)
            println("두 번째 코루틴: ${Thread.currentThread().name}")
        }
    }

    launch {  // 세 번째 코루틴
        println("세 번째 코루틴: ${Thread.currentThread().name}")
    }
}
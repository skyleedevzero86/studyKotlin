package com.koroutines.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * hi
 * Job: I'm sleeping 0 ...
 * Job: I'm sleeping 1 ...
 * Job: I'm sleeping 2 ...
 * main: I'm tired of waiting!
 * main: Now I can quit.
 *
 * 아래 예제에선 코루틴 중단이 됐을 때 cancel()로 취소될 수 있다.
 */
fun main() = runBlocking {

    val job = launch {
        repeat(1000) { i ->
            println("Job: I'm sleeping $i ...")
            delay(500L)
        }
    }

    println("hi")
    delay(1300L)
    println("main: I'm tired of waiting!")
    job.cancel() // 캔슬!
    job.join()
    println("main: Now I can quit.")
}
package com.koroutines.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

/**
 * job: I'm sleeping 0 ...
 * job: I'm sleeping 1 ...
 * job: I'm sleeping 2 ...
 * main: I'm tired of waiting!
 * Exception kotlinx.coroutines.JobCancellationException: StandaloneCoroutine was cancelled; job=StandaloneCoroutine{Cancelling}@61e24843
 * main: Now I can quit.
 *
 * 코루틴이 취소되기 위해선 협조적이어야 한다
 *
 * job.cancelAndJoin()이 호출되면, job 코루틴을 취소 요청.
 * 코루틴이 yield()를 호출하는 순간 취소됨.
 * 따라서 Exception 이 출력됨.
 * 코루틴이 정상 종료되지 않고, 취소되었음을 의미.
 */
fun main() = runBlocking {

    val startTime = System.currentTimeMillis()

    val job = launch(Dispatchers.Default) {
        try {
            var nextPrintTime = startTime
            var i = 0

            while (i < 5) {
                if (System.currentTimeMillis() >= nextPrintTime) {
                    yield()
                    println("job: I'm sleeping ${i++} ...")
                    nextPrintTime += 500L
                }
            }
        } catch (e: Exception) {
            println("Exception ${e}")
        }
    }

    delay(1300L)
    println("main: I'm tired of waiting!")
    job.cancelAndJoin()
    println("main: Now I can quit.")
}
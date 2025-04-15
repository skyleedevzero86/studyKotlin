package com.koroutines.coroutines

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/**
 * doSomethingUsefulOne()
 * doSomethingUsefulTwo()
 * The answer is 42
 * Completed in 1009 ms
 */

/**
 * 이건 async로 인해 즉시 실행되는 코루틴을 생성해서 동시에 실행된다. (단일 쓰레드에서 사용된다.)
 * async가 무조건 좋아 보이는데.. -> 작업의 순차적 실행이 필요할 땐 의미가 없어질 수 있다. 그리고 Lazy가 아니기 때문에 리소스 낭비가 있을 수 있음
 */
fun main() = runBlocking {

    val time = measureTimeMillis {
        val one = async { doSomethingUsefulOne2() }
        val two = async { doSomethingUsefulTwo2() }
        println("The answer is ${one.await() + two.await()}")
    }

    println("Completed in $time ms")
}

suspend fun doSomethingUsefulOne2(): Int {
    println("doSomethingUsefulOne()")
    delay(1000)
    return 13
}

suspend fun doSomethingUsefulTwo2(): Int {
    println("doSomethingUsefulTwo()")
    delay(1000)
    return 29
}
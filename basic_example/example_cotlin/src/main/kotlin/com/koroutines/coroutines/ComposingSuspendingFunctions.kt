package com.koroutines.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/**
 * doSomethingUsefulOne()
 * doSomethingUsefulTwo()
 * The answer is 42
 * Completed in 2016 ms
 */

/**
 * 여기선 순차 실행으로 2초가 됨
 * doSomethingOne이 호출되면 delay로 1초 중단이 되고, 이게 끝나야 two가 실행된다.
 */
fun main() = runBlocking {

    val time = measureTimeMillis {
        val one = doSomethingUsefulOne()
        val two = doSomethingUsefulTwo()
        println("The answer is ${one + two}")
    }

    println("Completed in $time ms")
}

suspend fun doSomethingUsefulOne(): Int {
    println("doSomethingUsefulOne()")
    delay(1000)
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    println("doSomethingUsefulTwo()")
    delay(1000)
    return 29
}
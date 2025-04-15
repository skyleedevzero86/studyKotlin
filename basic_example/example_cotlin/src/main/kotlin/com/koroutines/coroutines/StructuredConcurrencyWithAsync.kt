package com.koroutines.coroutines

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/**
 * doSomethingUsefulOne()
 * doSomethingUsefulTwo()
 * The answer is 42
 * Completed in 1027 ms
 */

fun main() = runBlocking {
    val time = measureTimeMillis {
        println("The answer is ${concurrentSum()}")
    }
    println("Completed in $time ms")
}

suspend fun concurrentSum(): Int = coroutineScope {
    val one = async { doSomethingUsefulOne4() }
    val two = async { doSomethingUsefulTwo4() }
    one.await() + two.await()
}

suspend fun doSomethingUsefulOne4(): Int {
    println("doSomethingUsefulOne()")
    delay(1000)
    return 13
}

suspend fun doSomethingUsefulTwo4(): Int {
    println("doSomethingUsefulTwo()")
    delay(1000)
    return 29
}
package com.koroutines.coroutines

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/**
 * doSomethingUsefulOne()
 * doSomethingUsefulTwo()
 * Exception
 * Exception in thread "main" java.lang.Exception
 */

// one -> two 나오고 Exception이 터지면 부모 코루틴까지 쭉 올라감
fun main() = runBlocking {
    val time = measureTimeMillis {
        println("The answer is ${concurrentSum2()}")
    }
    println("Completed in $time ms")
}

suspend fun concurrentSum2(): Int = coroutineScope {
    val one = async { doSomethingUsefulOne5() }
    val two = async { doSomethingUsefulTwo5() }

    delay(10)
    println("Exception")
    throw Exception()

    one.await() + two.await()
}

suspend fun doSomethingUsefulOne5(): Int {
    println("doSomethingUsefulOne()")
    delay(1000)
    return 13
}

suspend fun doSomethingUsefulTwo5(): Int {
    println("doSomethingUsefulTwo()")
    delay(1000)
    return 29
}

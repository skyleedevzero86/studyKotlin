package com.koroutines.coroutines

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun main() = runBlocking {

    val time = measureTimeMillis {
        val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne3() }
        val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo3() }

//        // start가 있으면 1초 걸리고 주석을 풀면 2초가 걸린다.
//        // 왜냐? 밑에서 호출하는 시점에서 start를 하므로 이런 결과가 나옴 (async를 쓸 때 이는 언제 쓸 수 있을지?, 개발자에게 자유로운 개발 선택지를 주는 건 아닐까 싶음)
//        one.start()
//        two.start()

        println("The answer is ${one.await() + two.await()}")
    }

    println("Completed in $time ms")
}

suspend fun doSomethingUsefulOne3(): Int {
    println("doSomethingUsefulOne()")
    delay(1000)
    return 13
}

suspend fun doSomethingUsefulTwo3(): Int {
    println("doSomethingUsefulTwo()")
    delay(1000)
    return 29
}
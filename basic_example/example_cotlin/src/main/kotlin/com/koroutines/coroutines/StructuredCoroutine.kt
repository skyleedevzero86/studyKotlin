package com.koroutines.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Coroutine Outer
 * Coroutine A, 0 ...
 * Coroutine B, 0 ...
 * Coroutine B, 1 ...
 * Coroutine B, 2 ...
 * Coroutine B, 3 ...
 * Coroutine B, 4 ...
 * Coroutine A, 1 ...
 * Coroutine A, 2 ...
 * Coroutine A, 3 ...
 * Coroutine A, 4 ...
 *
 * A에서 delay로 인해서 중단될 수 있다! 인텔리제이에선 중단 화살표 모양이 나옴
 *
 * delay()를 만나는 순간 suspension 돼서 다른 코루틴에게 실행 기회를 넘긴다.
 * !! B에는 중단 함수가 없기 때문에 실행 기회가 주어지면 쭉 실행된다.
 * 반대로 B에도 중단 함수를 추가해주면 A, B 둘다 Suspension이 반복되므로 서로에게 기회를 주고 받는다.
 */

fun main() = runBlocking {

    launch {
        repeat(5) { i ->
            println("Coroutine A, $i ...")
            delay(10L) // 중단 함수
        }
    }

    launch {
        repeat(5) { i ->
            println("Coroutine B, $i ...")
        }
    }

    println("Coroutine Outer")
}
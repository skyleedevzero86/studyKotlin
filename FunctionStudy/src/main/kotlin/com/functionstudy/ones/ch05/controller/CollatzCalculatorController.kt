package com.functionstudy.ones.ch05.controller

import com.functionstudy.ones.ch05.domain.collatz

class CollatzCalculatorController {
    fun execute() {

        // 확장 함수를 사용하여 코드 간소화
        val result13 = 13.collatz()
        val result8 = 8.collatz()

        println("Collatz sequence for 13: $result13")
        println("Collatz sequence for 8: $result8")

        println("\n==== 결과 분석 ====")
        println("13의 Collatz 수열 길이: ${result13.size}개 항목")
        println("8의 Collatz 수열 길이: ${result8.size}개 항목")
        println("13의 Collatz 수열 최대값: ${result13.maxOrNull()}")
    }
}

fun main() {
    val controller = CollatzCalculatorController()
    controller.execute()  // 결과 출력
}

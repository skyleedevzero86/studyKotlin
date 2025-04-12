package com.functionstudy.ones.ch10.controller

import com.functionstudy.ones.ch10.core.Logger

class LoggerMonadController {
    fun execute() {
        // Logger 모나드 사용
        val result = Logger(10, listOf("초기값: 10"))
            .bind { value -> Logger(value * 2, listOf("값을 2배로 증가: ${value * 2}")) }
            .bind { value -> Logger(value + 5, listOf("값에 5 추가: ${value + 5}")) }

        println("최종 값: ${result.value}")
        println("로그:")
        result.log.forEach { println("- $it") }

        // 모나드 법칙 테스트 실행
        testMonadLaws()
        println("모든 모나드 법칙 테스트 통과!")
    }
    fun testMonadLaws() {
        // 테스트에 사용할 함수들
        val f: (Int) -> Logger<Int> = { value -> Logger(value * 2, listOf("곱하기 2")) }
        val g: (Int) -> Logger<Int> = { value -> Logger(value + 1, listOf("더하기 1")) }

        // 테스트할 모나드 인스턴스
        val m = Logger(2, emptyList())

        // 1. 왼쪽 항등성 법칙
        val leftIdentity1 = Logger.pure(2).bind(f)
        val leftIdentity2 = f(2)
        assert(leftIdentity1 == leftIdentity2) { "왼쪽 항등성 법칙 실패" }

        // 2. 오른쪽 항등성 법칙
        val rightIdentity1 = m.bind(Logger.Companion::pure)
        val rightIdentity2 = m
        assert(rightIdentity1 == rightIdentity2) { "오른쪽 항등성 법칙 실패" }

        // 3. 결합 법칙
        val associativity1 = m.bind(f).bind(g)
        val associativity2 = m.bind { x -> f(x).bind(g) }
        assert(associativity1 == associativity2) { "결합 법칙 실패" }
    }
}
fun main() {
    val controller = LoggerMonadController()
    controller.execute()  // 결과 출력
}
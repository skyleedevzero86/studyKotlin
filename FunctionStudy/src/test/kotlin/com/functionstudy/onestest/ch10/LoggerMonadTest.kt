package com.functionstudy.onestest.ch10

import com.functionstudy.ones.ch10.core.Logger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

class LoggerMonadTest {

    private val f: (Int) -> Logger<Int> = { value -> Logger(value * 2, listOf("곱하기 2")) }
    private val g: (Int) -> Logger<Int> = { value -> Logger(value + 1, listOf("더하기 1")) }

    @Test
    @DisplayName("모나드 왼쪽 항등성 법칙 테스트")
    fun testLeftIdentityLaw() {
        // given
        println("주어진 값: 2")

        // when
        val left = Logger.pure(2).bind(f)
        val right = f(2)

        // then
        assertEquals(right, left, "왼쪽 항등성 법칙 실패")
        println("결과: 왼쪽 항등성 법칙 통과")
    }

    @Test
    @DisplayName("모나드 오른쪽 항등성 법칙 테스트")
    fun testRightIdentityLaw() {
        // given
        val m = Logger(2, emptyList())
        println("주어진 값: $m")

        // when
        val left = m.bind(Logger.Companion::pure)

        // then
        assertEquals(m, left, "오른쪽 항등성 법칙 실패")
        println("결과: 오른쪽 항등성 법칙 통과")
    }

    @Test
    @DisplayName("모나드 결합 법칙 테스트")
    fun testAssociativityLaw() {
        // given
        val m = Logger(2, emptyList())
        println("주어진 값: $m")

        // when
        val left = m.bind(f).bind(g)
        val right = m.bind { x -> f(x).bind(g) }

        // then
        assertEquals(right, left, "결합 법칙 실패")
        println("결과: 결합 법칙 통과")
    }

    @Test
    @DisplayName("Logger 모나드 실제 사용 예제 테스트")
    fun testLoggerUsageExample() {
        // given
        val initialLogger = Logger(10, listOf("초기값: 10"))
        println("주어진 값: 초기값 10")

        // when
        val result = initialLogger
            .bind { value -> Logger(value * 2, listOf("값을 2배로 증가: ${value * 2}")) }
            .bind { value -> Logger(value + 5, listOf("값에 5 추가: ${value + 5}")) }

        // then
        assertEquals(25, result.value)
        assertEquals(
            listOf("초기값: 10", "값을 2배로 증가: 20", "값에 5 추가: 25"),
            result.log
        )
        println("결과: 최종값은 ${result.value}이며, 로그는 ${result.log}입니다.")
    }
}
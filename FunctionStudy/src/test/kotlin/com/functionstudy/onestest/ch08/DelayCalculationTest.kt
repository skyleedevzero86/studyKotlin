package com.functionstudy.onestest.ch08

import com.functionstudy.ch08.controller.sumOfOddSquaresWithSequence
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals

class DelayCalculationTest {

    @Test
    @DisplayName("홀수 제곱의 합을 시퀀스를 사용하여 테스트")
    fun testSumOfOddSquaresWithSequence() {
        val numbers = (1L..1000L).asSequence()
        val result = sumOfOddSquaresWithSequence(numbers)
        println("결과: $result")  // 결과 출력
        assertEquals(166667000, result)
    }
}
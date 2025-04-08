package com.functionstudy.onestest.ch05

import com.functionstudy.ones.ch05.domain.collatz
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@DisplayName("Collatz 수열 계산 테스트")
class CollatzCalculatorTest {

    @Test
    @DisplayName("주어진 숫자에 대해 올바른 수열을 생성하는지 검증")
    fun `주어진 숫자에 대한 수열 생성 테스트`() {
        // Given: 테스트할 입력 값과 기대하는 결과 값 설정
        val input1 = 13
        val input2 = 8
        val expectedSequenceFor13 = listOf(13, 40, 20, 10, 5, 16, 8, 4, 2, 1)
        val expectedSequenceFor8 = listOf(8, 4, 2, 1)

        // When: 실제 수열을 계산
        val resultSequenceFor13 = input1.collatz()
        val resultSequenceFor8 = input2.collatz()

        // Then: 계산 결과가 기대한 값과 일치하는지 검증
        println("Collatz sequence for 13: $resultSequenceFor13") // 결과 출력
        println("Collatz sequence for 8: $resultSequenceFor8") // 결과 출력

        expectThat(resultSequenceFor13).isEqualTo(expectedSequenceFor13)
        expectThat(resultSequenceFor8).isEqualTo(expectedSequenceFor8)
    }
}
package com.functionstudy.onestest.ch07

import com.functionstudy.ch07.domain.Holder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@DisplayName("Holder 결합 테스트")
class HolderCombinationTest {

    @Test
    @DisplayName("두 Holder를 결합하여 새로운 Holder를 생성한다.")
    fun `두 Holder 결합 성공`() {
        // Given
        val h = Holder("hello")
        val w = Holder("world")

        // When
        val hw = h.combine(w) { a, b -> "$a $b" }
        println("결과: $hw") // 실제 값 확인용 출력

        // Then
        expectThat(hw).isEqualTo(Holder("hello world"))
    }

    @Test
    @DisplayName("다른 값과 비교하여 실패하는 경우")
    fun `두 Holder 결합 실패`() {
        // Given
        val h = Holder("hello")
        val w = Holder("wor1d")

        // When
        val hw = h.combine(w) { a, b -> "$a $b" }
        println("결과: $hw") // 실제 값 확인용 출력

        // Then (의도적으로 실패하게)
        expectThat(hw).isEqualTo(Holder("hello_world")) // 일부러 틀린 값 비교
    }
}

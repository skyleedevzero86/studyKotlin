package com.functionstudy.ch05

import com.functionstudy.ch03.utils.expectThat
import com.functionstudy.ch05.domain.Money
import com.functionstudy.ch05.domain.Monoid
import com.functionstudy.ch05.domain.zeroMoney
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("모노이드 연산 테스트")
class MonoidTest {

    @Test
    @DisplayName("Int 모노이드 연산이 올바르게 동작하는지 검증")
    fun `verify monoid of Int`() {
        // Given: Int 모노이드 및 리스트
        with(Monoid(0, Int::plus)) {
            // When: fold를 사용해 합산 결과를 얻음
            val result = listOf(1, 2, 3, 4, 10).fold()
            // Then: 결과는 20이어야 한다
            expectThat(result).isEqualTo(20)
            println("Int 모노이드 합산 결과: $result")  // 결과 출력
        }
    }

    @Test
    @DisplayName("String 모노이드 연산이 올바르게 동작하는지 검증")
    fun `verify monoid of String`() {
        // Given: String 모노이드 및 리스트
        with(Monoid("", String::plus)) {
            // When: fold를 사용해 문자열을 결합
            val result = listOf("My", "Fair", "Lady").fold()
            // Then: 결과는 "MyFairLady"이어야 한다
            expectThat(result).isEqualTo("MyFairLady")
            println("String 모노이드 결합 결과: $result")  // 결과 출력
        }
    }

    @Test
    @DisplayName("Money 모노이드 연산이 올바르게 동작하는지 검증")
    fun `verify monoid of Money`() {
        // Given: Money 모노이드 및 리스트
        with(Monoid(zeroMoney, Money::sum)) {
            // When: fold를 사용해 금액을 합산
            val result = listOf(
                Money(2.1),
                Money(3.9),
                Money(4.0)
            ).fold()
            // Then: 결과는 10 이어야 한다
            expectThat(result).isEqualTo(Money(10.0))
            println("Money 모노이드 합산 결과: $result")  // 결과 출력
        }
    }
}

package com.functionstudy.onestest.ch011

import com.functionstudy.ones.ch07.inter.OutcomeError
import com.functionstudy.ones.ch07.inter.asSuccess
import com.functionstudy.ones.ch07.inter.asFailure
import com.functionstudy.ones.ch07.domain.Outcome
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import com.functionstudy.ones.ch07.domain.reduceSuccess

class OutcomeExtensionsTest {

    // OutcomeError 구현체 정의
    data class 기본오류(override val msg: String) : OutcomeError

    @Test
    @DisplayName("성공 결과들을 모두 합산하여 하나의 성공 결과로 반환한다.")
    fun `given 성공 결과 리스트 when reduceSuccess 호출 then 합산된 성공 결과 반환`() {
        val list = listOf(
            1.asSuccess(),
            2.asSuccess(),
            3.asSuccess()
        )

        val result = list.reduceSuccess { acc, next -> acc + next }

        // 결과 출력
        println("합산된 결과: $result")

        expectThat(result).isEqualTo(6.asSuccess())
    }

    @Test
    @DisplayName("실패 결과가 포함된 리스트에서 reduceSuccess 호출 시 첫 번째 실패 결과를 반환한다.")
    fun `given 실패 결과 포함 리스트 when reduceSuccess 호출 then 첫 번째 실패 결과 반환`() {
        val list = listOf(
            1.asSuccess(),
            기본오류("오류 발생").asFailure(),
            3.asSuccess()
        )

        val result: Outcome<OutcomeError, Int> = list.reduceSuccess { acc, next -> acc + next }

        // 결과 출력
        println("첫 번째 실패 결과: $result")

        expectThat(result).isEqualTo(기본오류("오류 발생").asFailure())
    }
}

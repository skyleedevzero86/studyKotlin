package com.functionstudy.ones.ch011.controller

import com.functionstudy.ones.ch011.core.reduceSuccess
import com.functionstudy.ones.ch07.domain.Outcome
import com.functionstudy.ones.ch07.inter.GenericOutcomeError


class OutcomeExtensionsController {
    fun execute() {

        // Outcome.Success 예시 리스트
        val outcomes = listOf(
            Outcome.Success(1),
            Outcome.Success(2),
            Outcome.Success(3)
        )

        // reduceSuccess 테스트
        val result = outcomes.reduceSuccess { acc, value -> acc + value }
        println(result)  // 결과 출력

        // Outcome.Failure 예시
        val failureOutcomes = listOf(
            Outcome.Success(1),
            Outcome.Failure(GenericOutcomeError(Exception("오류가 발생했습니다"))),
            Outcome.Success(3)
        )

        val failureResult = failureOutcomes.reduceSuccess { acc, value -> acc + value }
        println(failureResult)  // 실패한 Outcome을 처리한 결과
    }
}

fun main() {
    val controller = OutcomeExtensionsController()
    controller.execute()  // 결과 출력
}

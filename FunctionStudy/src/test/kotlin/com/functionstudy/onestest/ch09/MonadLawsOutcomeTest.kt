package com.functionstudy.onestest.ch09

import com.functionstudy.ones.ch09.core.result.Outcome
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class MonadLawsOutcomeTest {

    fun add3(x: Int): Outcome<Int> = Outcome.Success(x + 3)
    val value = 7

    @Test
    fun `Outcome - Left identity`() {

        println("값 $value 를 포함한 Outcome 생성")

        val left = Outcome.Success(value).flatMap(::add3)
        println("왼쪽 flatMap 결과: $left")

        val right = add3(value)
        println("오른쪽 add3 결과: $right")

        // when&Then
        expectThat(left).isEqualTo(right)
        println("왼쪽과 오른쪽 결과가 같음: $left == $right")
    }

    @Test
    fun `Outcome - Right identity`() {
        // Given
        println("Given: 값 $value 를 포함한 Outcome 생성")
        val outcome = Outcome.Success(value)
        println("Given: Outcome 생성: $outcome")

        val left = outcome.flatMap { Outcome.Success(it) }
        println("Given: 왼쪽 flatMap 결과: $left")

        // when&Then
        expectThat(left).isEqualTo(outcome)
        println("Then: 왼쪽 flatMap 결과와 원본 Outcome이 같음: $left == $outcome")
    }

    @Test
    fun `Outcome - Associativity`() {
        // Given
        println("값 $value 를 포함한 Outcome 생성")

        val f: (Int) -> Outcome<Int> = { Outcome.Success(it * 2) }
        val g: (Int) -> Outcome<Int> = { Outcome.Success(it + 10) }

        val left = Outcome.Success(value).flatMap(f).flatMap(g)
        println("왼쪽 flatMap(f) 후 flatMap(g) 결과: $left")

        val right = Outcome.Success(value).flatMap { x -> f(x).flatMap(g) }
        println("오른쪽 flatMap(f) 후 flatMap(g) 결과: $right")

        // when&Then
        expectThat(left).isEqualTo(right)
        println("왼쪽과 오른쪽 결과가 같음: $left == $right")
    }
}
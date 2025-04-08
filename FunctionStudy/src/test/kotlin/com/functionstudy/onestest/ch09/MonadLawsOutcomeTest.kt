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
        val left = Outcome.Success(value).flatMap(::add3)
        val right = add3(value)

        expectThat(left).isEqualTo(right)
    }

    @Test
    fun `Outcome - Right identity`() {
        val outcome = Outcome.Success(value)
        val left = outcome.flatMap { Outcome.Success(it) }

        expectThat(left).isEqualTo(outcome)
    }

    @Test
    fun `Outcome - Associativity`() {
        val f: (Int) -> Outcome<Int> = { Outcome.Success(it * 2) }
        val g: (Int) -> Outcome<Int> = { Outcome.Success(it + 10) }

        val left = Outcome.Success(value).flatMap(f).flatMap(g)
        val right = Outcome.Success(value).flatMap { x -> f(x).flatMap(g) }

        expectThat(left).isEqualTo(right)
    }
}

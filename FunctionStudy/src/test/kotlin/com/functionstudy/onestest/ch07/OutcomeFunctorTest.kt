package com.functionstudy.onestest.ch07

import com.functionstudy.ones.ch07.controller.identity
import com.functionstudy.ones.ch07.controller.randomOutcome
import com.functionstudy.ones.ch07.controller.randomString
import com.functionstudy.ones.ch07.domain.Success
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@DisplayName("Outcome Functor 법칙 검증")
class OutcomeFunctorTest {

    @Test
    @DisplayName("Functor 동일성 법칙 검증")
    fun `펑터 동일성 법칙 검증`() {
        repeat(1000) {
            val randomOutcome = randomOutcome<String>()

            val transformedOutcome = randomOutcome.transform(::identity)

            println("Before: $randomOutcome, After: $transformedOutcome")

            expectThat(transformedOutcome).isEqualTo(randomOutcome)
        }
    }

    @Test
    @DisplayName("Functor 합성 법칙 검증")
    fun `펑터 합성 법칙 검증`() {
        repeat(1000) {
            val str = randomString()
            val f = { s: String -> s.length }
            val g = { len: Int -> len * 2 }
            val fThenG = { s: String -> g(f(s)) }

            val outcome1 = Success(str).transform(f).transform(g)
            val outcome2 = Success(str).transform(fThenG)

            println("Outcome1: $outcome1, Outcome2: $outcome2")

            expectThat(outcome1).isEqualTo(outcome2)
        }
    }
}

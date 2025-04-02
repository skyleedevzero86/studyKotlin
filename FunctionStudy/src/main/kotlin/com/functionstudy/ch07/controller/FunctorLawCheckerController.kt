package com.functionstudy.ch07.controller

import com.functionstudy.ch07.domain.Outcome
import com.functionstudy.ch07.domain.Success
import com.functionstudy.ch07.inter.OutcomeError
import java.time.Instant
import java.util.*

class FunctorLawCheckerController {
    fun execute() {
        // 1. 항등 함수 보존 테스트
        repeat(10) {
            val outcome = randomOutcome<String>()
            println("항등 함수 보존: ${verifyIdentityLaw(outcome)}")
        }

        // 2. 함수 합성 보존 테스트
        repeat(10) {
            val str = randomString()
            val f = { s: String -> s.length }
            val g = { len: Int -> len * 2 }
            println("함수 합성 보존: ${verifyCompositionLaw(str, f, g)}")
        }
    }
}

fun randomInstant(): Instant = Instant.ofEpochMilli(Random().nextLong())

val random = Random()

fun randomString(): String {
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val stringLength = random.nextInt(20) + 1 // 최소 길이 보장
    return (1..stringLength)
        .map { chars[random.nextInt(chars.length)] }
        .joinToString("")
}

inline fun <reified T> randomOutcome(): Outcome<OutcomeError, T> {
    return when (T::class) {
        Int::class -> Success(random.nextInt()) as Outcome<OutcomeError, T>
        String::class -> Success(randomString()) as Outcome<OutcomeError, T>
        Instant::class -> Success(randomInstant()) as Outcome<OutcomeError, T>
        Boolean::class -> Success(random.nextBoolean()) as Outcome<OutcomeError, T>
        else -> throw IllegalArgumentException("Unsupported type")
    }
}

// 1. 항등 함수 보존 확인
fun <T> verifyIdentityLaw(outcome: Outcome<OutcomeError, T>): Boolean {
    return outcome.transform(::identity) == outcome
}

// 2. 함수 합성 보존 확인
fun <A, B, C> verifyCompositionLaw(
    value: A,
    f: (A) -> B,
    g: (B) -> C
): Boolean {
    val fThenG = { a: A -> g(f(a)) }
    val outcome1 = Success(value).transform(f).transform(g)
    val outcome2 = Success(value).transform(fThenG)
    return outcome1 == outcome2
}

fun <T> identity(x: T): T = x

fun main() {
    val controller = FunctorLawCheckerController()
    controller.execute()  // 결과 출력
}

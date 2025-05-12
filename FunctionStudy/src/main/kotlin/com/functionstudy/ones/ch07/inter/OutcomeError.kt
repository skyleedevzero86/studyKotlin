package com.functionstudy.ones.ch07.inter

import com.functionstudy.ones.ch07.domain.Outcome

interface OutcomeError {
    val msg: String
}

data class GenericOutcomeError(val exception: Throwable) : OutcomeError {
    override val msg: String = exception.message ?: "Unknown error"
}

// 확장 함수 정의
fun <T> T.asSuccess(): Outcome<Nothing, T> = Outcome.Success(this)

fun <E : OutcomeError> E.asFailure(): Outcome<E, Nothing> = Outcome.Failure(this)

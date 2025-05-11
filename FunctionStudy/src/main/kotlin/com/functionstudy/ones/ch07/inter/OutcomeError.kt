package com.functionstudy.ones.ch07.inter

import com.functionstudy.ones.ch07.domain.Outcome

// OutcomeError 인터페이스 정의
interface OutcomeError {
    val msg: String
}

// asSuccess와 asFailure 확장 함수 정의
fun <T> T.asSuccess(): Outcome<Nothing, T> = Outcome.Success(this)
fun <E : OutcomeError> E.asFailure(): Outcome<E, Nothing> = Outcome.Failure(this)

// OutcomeError를 구현하는 GenericError 클래스 정의
data class GenericOutcomeError(val throwable: Throwable) : OutcomeError {
    override val msg: String = throwable.localizedMessage
}

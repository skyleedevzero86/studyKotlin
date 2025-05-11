package com.functionstudy.ones.ch07.domain

import com.functionstudy.ones.ch07.failure.EmailError
import com.functionstudy.ones.ch07.failure.FileReader
import com.functionstudy.ones.ch07.inter.GenericOutcomeError
import com.functionstudy.ones.ch07.inter.OutcomeError
import com.functionstudy.ones.ch07.inter.SimpleOutcomeError

sealed class Outcome<out E : OutcomeError, out T> {

    data class Success<T>(val value: T) : Outcome<Nothing, T>()
    data class Failure<E : OutcomeError>(val error: E) : Outcome<E, Nothing>()

    fun <U> transform(f: (T) -> U): Outcome<E, U> =
        when (this) {
            is Success -> Success(f(value))
            is Failure -> this
        }

    fun <F : OutcomeError> transformFailure(f: (E) -> F): Outcome<F, T> =
        when (this) {
            is Success -> this
            is Failure -> Failure(f(error))
        }
}

fun <E : OutcomeError, T, R> Outcome<E, T>.bind(f: (T) -> Outcome<E, R>): Outcome<E, R> =
    when (this) {
        is Outcome.Success -> f(this.value)
        is Outcome.Failure -> this
    }

fun <T> List<Outcome<*, T>>.reduceSuccess(operation: (T, T) -> T): Outcome<*, T> {
    var accumulator: T? = null

    for (outcome in this) {
        when (outcome) {
            is Outcome.Success -> {
                accumulator = if (accumulator == null) {
                    outcome.value
                } else {
                    operation(accumulator, outcome.value)
                }
            }
            is Outcome.Failure -> return outcome  // 실패가 있으면 바로 그 실패를 반환
        }
    }

    return if (accumulator != null) Outcome.Success(accumulator)
    else Outcome.Failure(SimpleOutcomeError("No success values"))
}

fun <E : OutcomeError, T, R> Outcome<E, T>.fold(success: (T) -> R, failure: (E) -> R): R =
    when (this) {
        is Outcome.Success -> success(this.value)
        is Outcome.Failure -> failure(this.error)
    }

fun <E : OutcomeError, T> Outcome<E, T>.recover(f: (E) -> T): Outcome<Nothing, T> =
    when (this) {
        is Outcome.Success -> this
        is Outcome.Failure -> Outcome.Success(f(this.error))
    }

fun <T> tryAndCatch(block: () -> T): Outcome<GenericOutcomeError, T> =
    try {
        block().asSuccess()
    } catch (e: Throwable) {
        GenericOutcomeError(e).asFailure()
    }

fun sendEmail(fileName: String): Outcome<EmailError, Unit> =
    FileReader.readFile(fileName)
        .transformFailure { EmailError("파일 읽기 오류: ${it.msg}") }
        .fold(
            success = { content -> EmailSender.sendTextByEmail(content) },
            failure = { error -> Outcome.Failure(error) }
        )

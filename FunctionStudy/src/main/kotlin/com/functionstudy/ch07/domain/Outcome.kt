package com.functionstudy.ch07.domain

import com.functionstudy.ch07.failure.OutcomeError
import com.functionstudy.ch07.failure.ThrowableError

sealed class Outcome<out E : OutcomeError, out T> {

    fun <U> transform(f: (T) -> U): Outcome<E, U> =
        when (this) {
            is Success -> Success(f(value))
            is Failure -> this
        }

    fun <F : OutcomeError> transformFailure(f: (E) -> F): Outcome<F, T> =
        when (this) {
            is Success -> this
            is Failure -> f(error).asFailure()
        }
}

data class Success<T>(val value: T) : Outcome<Nothing, T>()

data class Failure<E : OutcomeError>(val error: E) : Outcome<E, Nothing>()

inline fun <T, E : OutcomeError> Outcome<E, T>.onFailure(exitBlock: (E) -> Nothing): T =
    when (this) {
        is Success -> value
        is Failure -> exitBlock(error)
    }

fun <T> tryAndCatch(block: () -> T): Outcome<ThrowableError, T> {
    return try {
        Success(block())
    } catch (t: Throwable) {
        Failure(ThrowableError.Generic(t))
    }
}
package com.functionstudy.ch07.domain

import com.functionstudy.ch07.failure.OutcomeError
import com.functionstudy.ch07.failure.ThrowableError


sealed class Outcome<out E : OutcomeError, out T> {

    fun <U> transform(f: (T) -> U): Outcome<E, U> =
        when (this) {
            is Success -> f(value).asSuccess()
            is Failure -> this
        }

    fun <F : OutcomeError> transformFailure(f: (E) -> F): Outcome<F, T> =
        when (this) {
            is Success -> this
            is Failure -> f(error).asFailure()
        }


}

data class Failure<E : OutcomeError> internal constructor(val error: E) : Outcome<E, Nothing>()
data class Success<T> internal constructor(val value: T) : Outcome<Nothing, T>()


inline fun <T, E : OutcomeError> Outcome<E, T>.onFailure(exitBlock: (E) -> Nothing): T =
    when (this) {
        is Success<T> -> value
        is Failure<E> -> exitBlock(error)
    }

fun <T> tryAndCatch(block: () -> T): Outcome<ThrowableError, T> {
    return try {
        block().asSuccess()
    } catch (t: Throwable) {
        ThrowableError.Generic(t).asFailure()
    }
}

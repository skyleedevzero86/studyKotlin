package com.functionstudy.ones.ch09.core.result

sealed class Outcome<out T> {
    data class Success<T>(val value: T) : Outcome<T>()
    data class Failure(val reason: String) : Outcome<Nothing>()

    fun <U> map(f: (T) -> U): Outcome<U> = when (this) {
        is Success -> Success(f(value))
        is Failure -> this
    }

    fun <U> flatMap(f: (T) -> Outcome<U>): Outcome<U> = when (this) {
        is Success -> f(value)
        is Failure -> this
    }
}

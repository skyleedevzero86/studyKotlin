package com.komroonga.global.utils

// init용
typealias InitializationResult = Result<Unit>
typealias Username = String
typealias Password = String
typealias MemberData = Pair<Username, Password>

typealias Try<T> = Result<T>

typealias AsyncOperation<T> = suspend () -> Try<T>

typealias Transformer<T, R> = (T) -> R

fun interface Validator<T> {
    fun validate(value: T): Try<T>
}

fun interface BusinessRule<T> {
    fun check(value: T): Boolean
}

fun interface EffectfulOperation<T, R> {
    suspend fun perform(input: T): Try<R>
}

inline fun <T, R> Try<T>.map(crossinline transform: (T) -> R): Try<R> =
    fold(
        onSuccess = { Result.success(transform(it)) },
        onFailure = { Result.failure(it) }
    )

inline fun <T, R> Try<T>.flatMap(crossinline transform: (T) -> Try<R>): Try<R> =
    fold(
        onSuccess = { transform(it) },
        onFailure = { Result.failure(it) }
    )
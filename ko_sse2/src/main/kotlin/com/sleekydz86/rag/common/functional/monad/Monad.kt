package com.sleekydz86.rag.common.functional.monad

sealed class Either<out L, out R> {
    data class Left<L>(val value: L) : Either<L, Nothing>()
    data class Right<R>(val value: R) : Either<Nothing, R>()

    inline fun <T> fold(ifLeft: (L) -> T, ifRight: (R) -> T): T = when (this) {
        is Left -> ifLeft(value)
        is Right -> ifRight(value)
    }

    inline fun <T> map(transform: (R) -> T): Either<@UnsafeVariance L, T> = when (this) {
        is Left -> this
        is Right -> Right(transform(value))
    }

    inline fun <T> flatMap(transform: (R) -> Either<@UnsafeVariance L, T>): Either<L, T> = when (this) {
        is Left -> this
        is Right -> transform(value)
    }
}

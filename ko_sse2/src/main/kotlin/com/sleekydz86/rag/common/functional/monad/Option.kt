package com.sleekydz86.rag.common.functional.monad

sealed class Option<out T> {
    object None : Option<Nothing>()
    data class Some<T>(val value: T) : Option<T>()

    inline fun <R> map(transform: (T) -> R): Option<R> = when (this) {
        is None -> None
        is Some -> Some(transform(value))
    }

    inline fun <R> flatMap(transform: (T) -> Option<R>): Option<R> = when (this) {
        is None -> None
        is Some -> transform(value)
    }

    inline fun getOrElse(default: () -> @UnsafeVariance T): T = when (this) {
        is None -> default()
        is Some -> value
    }

    fun isEmpty(): Boolean = this is None
    fun isDefined(): Boolean = this is Some

    fun getOrNull(): T? = when (this) {
        is None -> null
        is Some -> value
    }
}
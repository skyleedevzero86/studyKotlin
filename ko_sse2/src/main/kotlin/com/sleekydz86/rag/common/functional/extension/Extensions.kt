package com.sleekydz86.rag.common.functional.extension

import com.sleekydz86.rag.common.functional.monad.*

fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    is Result.Error -> null
}

fun <T> Result<T>.getOrElse(defaultValue: (String) -> T): T = when (this) {
    is Result.Success -> data
    is Result.Error -> defaultValue(message)
}

fun String.isNotEmpty(): Boolean = this.trim().isNotEmpty()

infix fun <T, R> T.pipe(transform: (T) -> R): R = transform(this)

fun <T> T?.toOption(): Option<T> = if (this != null) Option.Some(this) else Option.None
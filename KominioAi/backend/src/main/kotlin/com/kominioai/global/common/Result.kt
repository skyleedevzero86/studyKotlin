package com.kominioai.global.common

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure(val error: Throwable) : Result<Nothing>()
    
    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }
    
    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Failure -> this
    }
    
    fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }
    
    fun onFailure(action: (Throwable) -> Unit): Result<T> {
        if (this is Failure) action(error)
        return this
    }
} 
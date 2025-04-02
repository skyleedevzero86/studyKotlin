package com.functionstudy.ch07.failure

sealed class ThrowableError : OutcomeError {
    data class Generic(val throwable: Throwable) : ThrowableError()
}

interface OutcomeError
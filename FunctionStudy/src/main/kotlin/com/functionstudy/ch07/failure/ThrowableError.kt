package com.functionstudy.ch07.failure

import com.functionstudy.ch07.inter.OutcomeError

sealed class ThrowableError : OutcomeError {
    data class Generic(val throwable: Throwable) : ThrowableError()
}
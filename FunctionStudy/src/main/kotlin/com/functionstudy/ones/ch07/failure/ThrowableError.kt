package com.functionstudy.ones.ch07.failure

import com.functionstudy.ones.ch07.inter.OutcomeError

sealed class ThrowableError : OutcomeError {
    abstract val msg: String

    data class Generic(val throwable: Throwable) : ThrowableError() {
        override val msg: String get() = throwable.localizedMessage ?: "Unknown error"
    }
}

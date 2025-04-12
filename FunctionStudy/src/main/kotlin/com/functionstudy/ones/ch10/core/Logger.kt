package com.functionstudy.ones.ch10.core

data class Logger<T>(val value: T, val log: List<String>) {
    // transform은 펑터의 map 메소드와 같은 기능
    fun <U> transform(f: (T) -> U): Logger<U> = Logger(f(value), log)

    // 바인드는 모나드의 flatMap과 같은 기능
    fun <U> bind(f: (T) -> Logger<U>): Logger<U> = f(value).let {
        Logger(it.value, log + it.log)
    }

    companion object {
        // 모나드의 pure/return 함수
        fun <T> pure(value: T): Logger<T> = Logger(value, emptyList())
    }
}
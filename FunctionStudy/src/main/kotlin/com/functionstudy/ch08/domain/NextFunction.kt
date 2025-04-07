package com.functionstudy.ch08.domain

data class NextFunction<T>(val list: List<T>) : () -> T? {
    private var index = 0

    override fun invoke(): T? {
        return if (index < list.size) list[index++] else null
    }
}
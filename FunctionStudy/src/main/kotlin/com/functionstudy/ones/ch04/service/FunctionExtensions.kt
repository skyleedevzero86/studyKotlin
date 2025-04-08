package com.functionstudy.ones.ch04.service

fun <T, R, U> ((T) -> R?).andUnlessNull(other: (R) -> U?): (T) -> U? {
    return { input ->
        val result1 = this(input)
        if (result1 != null) {
            other(result1)
        } else {
            null
        }
    }
}
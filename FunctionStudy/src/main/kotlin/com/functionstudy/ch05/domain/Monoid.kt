package com.functionstudy.ch05.domain

data class Monoid<T : Any>(val zero: T, val combination: (T, T) -> T) {
    fun List<T>.fold(): T = fold(zero, combination)
}
package com.functionstudy.ones.ch05.domain

class CollatzCalculator {
    fun calculate(n: Int): List<Int> {
        tailrec fun collatzR(acc: List<Int>, x: Int): List<Int> = when {
            x == 1 -> acc + 1
            x % 2 == 0 -> collatzR(acc + x, x / 2)
            else -> collatzR(acc + x, x * 3 + 1)
        }
        return collatzR(emptyList(), n)
    }

}

fun Int.collatz(): List<Int> {
    val calculator = CollatzCalculator()
    return calculator.calculate(this)
}
package com.functionstudy.ch08.controller

class DelayCalculationController {
    fun execute() {
        val numbers = (1L..1_000_000L).toList()

        // For loop 방식
        val sumForLoop = sumOfOddSquaresForLoop(numbers)
        println("For loop sum of odd squares: $sumForLoop")

        // List 방식
        val sumWithList = sumOfOddSquaresWithList(numbers)
        println("List sum of odd squares: $sumWithList")

        // Sequence 방식
        val sumWithSequence = sumOfOddSquaresWithSequence(numbers.asSequence())
        println("Sequence sum of odd squares: $sumWithSequence")
    }
}

fun Long.isOdd(): Boolean = this % 2 != 0L

fun sumOfOddSquaresForLoop(numbers: List<Long>): Long {
    var tot = 0L
    for (i in numbers) {
        if (i.isOdd()) tot += i * i
    }
    return tot
}

fun sumOfOddSquaresWithList(numbers: List<Long>): Long {
    return numbers.filter { it.isOdd() }.map { it * it }.sum()
}

fun sumOfOddSquaresWithSequence(numbers: Sequence<Long>): Long {
    return numbers.filter { it.isOdd() }.map { it * it }.sum()
}

fun main() {
    val controller = DelayCalculationController()
    controller.execute()  // 결과 출력
}
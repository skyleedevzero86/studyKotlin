package com.functionstudy.ones.ch02.controller

import com.functionstudy.ch02.calculator.RPNCalculator

class RPNCalculatorController {
    fun execute() {
        val calculator = RPNCalculator()

        val expressions = listOf(
            "5 3 +",     // 8
            "7 2 -",     // 5
            "4 5 * 2 +", // 22
            "10 3 /"     // 3.33
        )

        expressions.forEach { expression ->
            try {
                val result = calculator.calculate(expression)
                println("$expression = $result")
            } catch (e: Exception) {
                println("오류: ${e.message}")
            }
        }
    }
}

fun main() {
    val controller = RPNCalculatorController()
    controller.execute()
}
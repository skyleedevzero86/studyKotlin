package com.functionstudy.ones.ch02.calculator

class RPNCalculator {

    fun calculate(expression: String): Double {
        val stack = mutableListOf<Double>()

        val tokens = expression.split(" ")

        for (token in tokens) {
            when (token) {
                "+", "-", "*", "/" -> {

                    require(stack.size >= 2) { "연산을 수행하기에 충분한 숫자가 없습니다." }

                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)

                    val result = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> a / b
                        else -> throw IllegalArgumentException("알 수 없는 연산자")
                    }
                    stack.add(result)
                }
                else -> {
                    stack.add(token.toDouble())
                }
            }
        }

        require(stack.size == 1) { "잘못된 RPN 표현식입니다." }
        return stack[0]
    }
}
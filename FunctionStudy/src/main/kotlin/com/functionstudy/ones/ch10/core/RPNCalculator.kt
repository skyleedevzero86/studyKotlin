package com.functionstudy.ones.ch10.core

class RPNCalculator {
    fun calculate(expression: String): Double {
        val stack = mutableListOf<Double>()
        val tokens = expression.split(" ")

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> stack.add(token.toDouble())  // 숫자일 경우 스택에 추가
                token == "+" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a + b)
                }
                token == "-" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a - b)
                }
                token == "*" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a * b)
                }
                token == "/" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a / b)
                }
                else -> throw IllegalArgumentException("잘못된 연산자: $token")
            }
        }

        return stack.firstOrNull() ?: throw IllegalArgumentException("잘못된 표현식")
    }
}
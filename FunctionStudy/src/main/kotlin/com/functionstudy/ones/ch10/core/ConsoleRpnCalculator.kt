package com.functionstudy.ones.ch10.core

fun ConsoleRpnCalculator(): ContextReader<ConsoleContext, String> {
    val rpnCalculator = RPNCalculator()

    return contextPrintln("RPN 표현식을 입력하여 결과를 계산하거나 Q를 입력하여 종료하세요.")
        .bind { _ -> contextReadln() }
        .bind { input ->
            if (input == "Q") {
                contextPrintln("Bye!")
            } else {
                return@bind try {
                    val result = rpnCalculator.calculate(input)
                    contextPrintln("결과: $result")
                } catch (e: Exception) {
                    contextPrintln("에러: ${e.message}")
                }
            }
        }
        .bind { msg ->
            if (msg == "Bye!") {
                contextPrintln("")
            } else {
                ConsoleRpnCalculator()
            }
        }
}
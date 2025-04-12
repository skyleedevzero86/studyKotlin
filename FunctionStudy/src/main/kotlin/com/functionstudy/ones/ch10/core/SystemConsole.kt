package com.functionstudy.ones.ch10.core

import java.io.BufferedReader
import java.io.InputStreamReader

class SystemConsole : ConsoleContext {
    override fun printLine(msg: String): String = msg.also { println(it) }
    private val reader = BufferedReader(InputStreamReader(System.`in`))
    override fun readLine(): String = reader.readLine()
}

fun contextPrintln(msg: String) = ContextReader<ConsoleContext, String> { ctx -> ctx.printLine(msg) }
fun contextReadln() = ContextReader<ConsoleContext, String> { ctx -> ctx.readLine() }

val calculator = RPNCalculator()

fun askContinue(): ContextReader<ConsoleContext, String> =
    contextPrintln("계속하시겠습니까? (Y/n)")
        .bind { _ -> contextReadln() }
        .bind { answer ->
            if (answer.equals("n", ignoreCase = true)) {
                contextPrintln("계산기를 종료합니다.").transform { "Bye!" }
            } else {
                consoleRpnLoop()
            }
        }

fun consoleRpnLoop(): ContextReader<ConsoleContext, String> =
    contextPrintln("RPN 수식을 입력해주세요 (종료: Q):")
        .bind { _ -> contextReadln() }
        .bind { input ->
            if (input.equals("Q", ignoreCase = true)) {
                contextPrintln("계산기를 종료합니다.").transform { "Bye!" }
            } else {
                try {
                    val result = calculator.calculate(input)
                    contextPrintln("결과: $result")
                        .bind { askContinue() }
                } catch (e: Exception) {
                    contextPrintln("에러: ${e.message}")
                        .bind { consoleRpnLoop() }
                }
            }
        }
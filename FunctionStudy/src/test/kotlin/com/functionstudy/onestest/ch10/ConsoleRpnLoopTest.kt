package com.functionstudy.onestest.ch10

import com.functionstudy.ones.ch10.core.ConsoleContext
import com.functionstudy.ones.ch10.core.consoleRpnLoop
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals

class ConsoleRpnLoopTest {

    class TestConsole(private val inputs: List<String>) : ConsoleContext {
        private val outputs = mutableListOf<String>()
        private var inputIndex = 0

        override fun printLine(msg: String): String {
            outputs.add(msg)
            return msg
        }

        override fun readLine(): String {
            return inputs.getOrElse(inputIndex++) { "Q" }
        }

        fun getOutputs(): List<String> = outputs
    }

    @Test
    @DisplayName("RPN 수식 계산기 테스트 - 정상 동작")
    fun testRpnCalculator() {
        // given
        val testInputs = listOf("5 1 2 + 4 * + 3 -", "n")
        val testConsole = TestConsole(testInputs)

        // when
        val result = consoleRpnLoop().runWith(testConsole)

        // then
        val outputs = testConsole.getOutputs()

        // 디버깅용 출력
        outputs.forEach { println(it) }
        assertEquals("Bye!", result)
        assert(outputs.any { it.contains("결과: 14.0") })
        assert(outputs.any { it.contains("계속하시겠습니까?") })
    }

    @Test
    @DisplayName("잘못된 수식 입력 시 예외 처리")
    fun testInvalidInput() {
        val testInputs = listOf("5 + +", "Q")
        val testConsole = TestConsole(testInputs)

        val result = consoleRpnLoop().runWith(testConsole)
        val outputs = testConsole.getOutputs()

        outputs.forEach { println(it) }

        assertEquals("Bye!", result)
        assert(outputs.any { it.contains("에러") })
    }
}

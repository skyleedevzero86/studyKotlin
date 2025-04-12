package com.functionstudy.onestest.ch10

import com.functionstudy.ones.ch10.core.ConsoleRpnCalculator
import com.functionstudy.ones.ch10.core.SystemConsole
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@DisplayName("RPN 계산기 콘솔 입출력 테스트")
class RpnCalculatorTest {

    @Test
    @DisplayName("RPN 계산기가 콘솔에서 읽고 쓰는지 검증")
    fun `RPN 계산기 콘솔에서 읽고 쓰는지 검증`() {
        // Given
        val output = ByteArrayOutputStream()
        val input = ByteArrayInputStream(
            """
            4 3 2 1 - + *
            1 2 3 * 4 - +
            Q
        """.trimIndent().toByteArray()
        )

        val stdOut = System.out
        val stdIn = System.`in`

        try {
            // 콘솔 입력과 출력을 테스트 스트림으로 설정
            System.setIn(input)
            System.setOut(PrintStream(output))

            // RPN 계산기 실행
            ConsoleRpnCalculator().runWith(SystemConsole())

        } finally {
            // 원래의 콘솔 입출력 스트림 복원
            System.setOut(stdOut)
            System.setIn(stdIn)
        }

        // Then
        val expected =
            """RPN 계산식을 입력하거나 종료하려면 Q를 입력하세요.
              |결과는: 16.0
              |RPN 계산식을 입력하거나 종료하려면 Q를 입력하세요.
              |결과는: 3.0
              |RPN 계산식을 입력하거나 종료하려면 Q를 입력하세요.
              |안녕!
              |
              |""".trimMargin()

        // 결과가 예상대로 출력되는지 검증
        expectThat(output.toString()).isEqualTo(expected)
    }
}
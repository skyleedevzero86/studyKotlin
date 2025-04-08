package com.functionstudy.onestest.ch02

import com.functionstudy.ch02.calculator.RPNCalculator
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RPNCalculatorTest {
    private val calculator = RPNCalculator()

    @Test
    fun `덧셈 테스트`() {
        assertEquals(8.0, calculator.calculate("5 3 +"))
    }

    @Test
    fun `뺄셈 테스트`() {
        assertEquals(2.0, calculator.calculate("5 3 -"))
    }

    @Test
    fun `곱셈 테스트`() {
        assertEquals(15.0, calculator.calculate("5 3 *"))
    }

    @Test
    fun `나눗셈 테스트`() {
        assertEquals(2.0, calculator.calculate("6 3 /"))
    }

    @Test
    fun `복합 연산 테스트`() {
        assertEquals(22.0, calculator.calculate("4 5 * 2 +"))
    }

    @Test
    fun `부족한 연산자 테스트`() {
        assertFailsWith<IllegalArgumentException> {
            calculator.calculate("5 +")
        }
    }

    @Test
    fun `잘못된 RPN 표현식 테스트`() {
        assertFailsWith<IllegalArgumentException> {
            calculator.calculate("5 3 + 2")
        }
    }
}
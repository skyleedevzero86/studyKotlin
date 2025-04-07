package com.functionstudy.ch08

import com.functionstudy.ch08.domain.NextFunction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals

class NextFunctionTest {

    @Test
    @DisplayName("NextFunction 상태 변경 테스트")
    fun testNextFunction() {
        val names = listOf("Ann", "Bob", "Charlie", "Dorothy")
        val nextName = NextFunction(names)

        println(nextName())  // "Ann" 출력
        println(nextName())  // "Bob" 출력
        println(nextName())  // "Charlie" 출력
        println(nextName())  // "Dorothy" 출력
        println(nextName())  // null 출력

        assertEquals("Ann", nextName())
        assertEquals("Bob", nextName())
        assertEquals("Charlie", nextName())
        assertEquals("Dorothy", nextName())
        assertEquals(null, nextName())
    }
}
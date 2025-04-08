package com.functionstudy.onestest.ch04

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import com.functionstudy.ch04.curry.curry
import com.functionstudy.ch04.curry.plusPlus

class CurryingTest {

    @Test
    fun testCurrying() {
        // Given: 초기 값 설정
        val sum: (Int, Int) -> Int = { num1, num2 -> num1 + num2 }
        val strConcat: (String, String) -> String = { s1, s2 -> "$s1$s2" }

        // When: 커링 함수 적용
        val plus3Fn = sum.curry()(3)
        val starPrefixFn = strConcat.curry()("*")

        val curriedConcat = strConcat.curry()
        val curriedSum = sum.curry()

        // Then: 결과 검증

        println("plus3Fn(4): ${plus3Fn(4)}")
        println("starPrefixFn(\"abc\"): ${starPrefixFn("abc")}")

        assertEquals(7, plus3Fn(4))
        assertEquals("*abc", starPrefixFn("abc"))

        println("curriedConcat +++ \"head\" +++ \"tail\": ${curriedConcat.plusPlus("head")("tail")}")
        println("curriedSum +++ 4 +++ 5: ${curriedSum.plusPlus(4)(5)}")

        assertEquals("head tail", curriedConcat.plusPlus("head")("tail"))
        assertEquals(9, curriedSum.plusPlus(4)(5))
    }
}

package com.functionstudy.ch04.test

import com.functionstudy.ch04.service.andUnlessNull  // 확장 함수 import
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertEquals

class AndUnlessNullTest {

    // andUnlessNull의 동작을 테스트하는 함수
    @Test
    fun testAndUnlessNull() {
        // Given: 두 개의 함수 정의
        val doubleToString: (Int) -> String = { it.toString() }
        val stringToInt: (String) -> Int? = { it.toIntOrNull() }

        // When: 두 함수를 연결하여 combined 함수 생성
        val combined = doubleToString.andUnlessNull(stringToInt)

        // Then: combined 함수가 null이 아니어야 함
        assertNotNull(combined)

        // Then: 42를 인자로 주어 combined 함수 호출
        assertNotNull(combined(42)) //결과는 42여야 함

        // Then: 42를 인자로 주어 combined 함수 호출, "abc"는 Int로 변환할 수 없음
        assertNull(combined(42)) //null이 반환되어야 함
    }

    // 다양한 값을 처리
    @Test
    fun testAndUnlessNullWithDifferentInputs() {
        // Given: 두 개의 함수 정의
        val intToString: (Int) -> String = { it.toString() }
        val stringToInt: (String) -> Int? = { it.toIntOrNull() }

        // When: 두 함수를 연결하여 combined 함수 생성
        val combined = intToString.andUnlessNull(stringToInt)

        // Then: 100을 인자로 주면 100이 반환되어야 함
        assertNotNull(combined(100))

        // Then:  100을 인자로 주고 combined 함수 호출
        assertNull(combined(100)) //null이 반환되어야 함
    }

    // 두 번째 함수가 항상 null을 반환하는 경우
    @Test
    fun testAndUnlessNullWithNullReturningFunction() {
        // Given: 두 개의 함수 정의, 두 번째 함수는 항상 null 반환
        val stringToString: (Int) -> String = { it.toString() }
        val alwaysNull: (String) -> Int? = { null }

        // When: 두 함수를 연결하여 combined 함수 생성
        val combined = stringToString.andUnlessNull(alwaysNull)

        // Then: 모든 입력에 대해 combined 함수는 null을 반환해야 함
        assertNull(combined(123))
    }

    // 두 번째 함수가 항상 유효한 값을 반환하는 경우
    @Test
    fun testAndUnlessNullWithAlwaysValidFunction() {
        // Given: 두 개의 함수 정의
        val stringToString: (Int) -> String = { it.toString() }
        val alwaysValid: (String) -> Int? = { it.length }

        // When: 두 함수를 연결하여 combined 함수 생성
        val combined = stringToString.andUnlessNull(alwaysValid)

        // Then: 입력값에 대해 항상 문자열 길이를 반환
        assertEquals(3, combined(123))  // "123" 길이는 3
        assertEquals(4, combined(4567))  // "4567" 길이는 4
    }

    // null 반환하는 첫 번째 함수
    @Test
    fun testAndUnlessNullWithNullFirstFunction() {
        // Given: 첫 번째 함수는 null 반환, 두 번째 함수는 String을 Int로 변환
        val nullReturning: (Int) -> String? = { null }
        val stringToInt: (String) -> Int? = { it.toIntOrNull() }

        // When: 두 함수를 연결하여 combined 함수 생성
        val combined = nullReturning.andUnlessNull(stringToInt)

        // Then: 첫 번째 함수가 null을 반환하면 전체 결과도 null이어야 함
        assertNull(combined(42))
    }
}

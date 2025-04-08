package com.functionstudy.onestest.ch05

import com.functionstudy.ch03.utils.expectThat
import com.functionstudy.ch05.service.compactJson
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("JSON 공백 제거 및 문자열 유지 테스트")
class UnionTypesTest {
    @Test
    @DisplayName("주어진 JSON 문자열에서 공백을 제거하고 문자열 리터럴은 유지한다")
    fun `문자열 리터럴 외의 공백만 제거하는 JSON 압축`() {
        // Given: 테스트할 JSON 문자열과 기대 결과
        val jsonText = """{ "my greetings" : "hello world! \"How are you?\"" }"""
        val expected = """{"my greetings":"hello world! \"How are you?\""}"""

        // When: compactJson 함수 호출하여 공백을 제거한 JSON 문자열 생성
        val result = compactJson(jsonText)

        // Then: 결과가 기대한 값과 일치하는지 확인
        println("Original JSON: $jsonText")
        println("Compacted JSON: $result")
        expectThat(result).isEqualTo(expected)
    }
}
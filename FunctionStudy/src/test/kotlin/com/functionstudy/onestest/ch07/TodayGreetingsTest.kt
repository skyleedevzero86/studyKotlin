package com.functionstudy.onestest.ch07

import com.functionstudy.ones.ch07.controller.TodayGreetingsController
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.startsWith

@DisplayName("날짜 변환 기능 테스트")
class TodayGreetingsTest {

    private val controller = TodayGreetingsController()

    @Test
    @DisplayName("정상적인 날짜 변환")
    fun `정상적인 날짜 변환`() {
        // Given
        val dateString = "2020-03-12"

        // When
        val result = controller.todayGreetings(dateString)

        // Then
        println("입력: $dateString, 결과: $result")
        expectThat(result.toString()).isEqualTo("Today is 2020-03-12")
    }

    @Test
    @DisplayName("잘못된 날짜 형식 입력")
    fun `잘못된 날짜 형식 입력`() {
        // Given
        val invalidDateString = "12/03/2020"

        // When
        val result = controller.todayGreetings(invalidDateString)

        // Then
        println("입력: $invalidDateString, 결과: $result")
        expectThat(result.toString()).startsWith("날짜 형식이 올바르지 않습니다.")
    }

    @Test
    @DisplayName("공백 문자열 입력")
    fun `공백 문자열 입력`() {
        // Given
        val emptyString = ""

        // When
        val result = controller.todayGreetings(emptyString)

        // Then
        println("입력: '$emptyString', 결과: $result")
        expectThat(result.toString()).startsWith("날짜를 변환하는 중 오류가 발생했습니다.")
    }

    @Test
    @DisplayName("null 입력 시 예외 처리")
    fun `null 입력 시 예외 처리`() {
        // Given
        val nullString: String? = null

        // When
        val result = controller.todayGreetings(nullString ?: "")

        // Then
        println("입력: null, 결과: $result")
        expectThat(result.toString()).startsWith("유효하지 않은 날짜 형식입니다.")
    }
}

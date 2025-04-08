package com.functionstudy.onestest.ch05

import com.functionstudy.ones.ch03.utils.expectThat
import com.functionstudy.ones.ch05.domain.Down
import com.functionstudy.ones.ch05.domain.Elevator
import com.functionstudy.ones.ch05.domain.Up
import com.functionstudy.ones.ch05.service.ElevatorService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("엘리베이터 수동 이벤트 계산 테스트")
class ElevatorServiceTest {

    private val elevatorService = ElevatorService()

    @Test
    @DisplayName("주어진 방향에 대한 엘리베이터 최종 위치 계산 테스트")
    fun `주어진 방향에 따라 엘리베이터 최종 위치를 계산한다`() {
        // Given: 엘리베이터의 방향 이벤트 리스트
        val directions = listOf(Up, Up, Down, Up, Down, Down, Up, Up, Up, Down)

        // When: fold를 사용하여 최종 층을 계산
        val result = elevatorService.calculateFinalFloor(directions)

        // Then: 최종 층수는 2여야 한다
        println("최종 엘리베이터 위치: ${result.floor}")
        expectThat(result).isEqualTo(Elevator(2))
    }
}
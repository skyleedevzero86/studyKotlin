package com.functionstudy.onestest.ch08

import com.functionstudy.ch08.core.ElevatorEvent
import com.functionstudy.ch08.core.ElevatorProjectionInMemory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals

class ElevatorProjectionTest {

    @Test
    @DisplayName("엘리베이터 프로젝션 메모리 내에서 테스트")
    fun testElevatorProjectionInMemory() {
        val events = listOf(
            ElevatorEvent.ButtonPressed(1, 2),
            ElevatorEvent.ElevatorMoved(1, 0, 2),
            ElevatorEvent.ButtonPressed(1, 5),
            ElevatorEvent.ElevatorMoved(1, 2, 5),
            ElevatorEvent.ElevatorBroken(1),
            ElevatorEvent.ElevatorFixed(1)
        )

        val projection = ElevatorProjectionInMemory(events)

        val row = projection.getRow(1)
        println("엘리베이터 ID: ${row?.elevatorId}, 층: ${row?.floor}")  // 결과 출력
        assertEquals(1, row?.elevatorId)
        assertEquals(0, row?.floor) // 엘리베이터가 고쳐진 후 지상층으로 돌아갔다고 가정
    }
}
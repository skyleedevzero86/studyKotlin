package com.functionstudy.ch08

import com.functionstudy.ones.ch08.core.ElevatorEvent
import com.functionstudy.ones.ch08.core.ElevatorProjectionInMemory

class ElevatorProjectionController {
    fun execute() {
        val events = listOf(
            ElevatorEvent.ButtonPressed(1, 2),
            ElevatorEvent.ElevatorMoved(1, 0, 2),
            ElevatorEvent.ButtonPressed(1, 5),
            ElevatorEvent.ElevatorMoved(1, 2, 5),
            ElevatorEvent.ElevatorBroken(1),
            ElevatorEvent.ElevatorFixed(1)
        )

        val projection = ElevatorProjectionInMemory(events)

        println(projection.allRows()) // 모든 행 출력
        println(projection.getRow(1)) // 엘리베이터 1의 행 출력
    }
}

fun main() {
    val controller = ElevatorProjectionController()
    controller.execute()  // 결과 출력
}
package com.functionstudy.ones.ch06.controller

import com.functionstudy.ch05.controller.MonoidController
import com.functionstudy.ch06.command.ElevatorCommand
import com.functionstudy.ch06.event.ElevatorEvent
import com.functionstudy.ch06.statemachine.ElevatorStateMachine

class ElevatorController {
    fun execute() {
        println("===== 엘리베이터 상태 기계 시뮬레이션 =====")

        var events = listOf<ElevatorEvent>()

        println("시나리오 1: 엘리베이터 5층으로 호출")
        events = ElevatorStateMachine.processCommand(events, ElevatorCommand.CallElevator(5))

        println("시나리오 2: 엘리베이터 10층으로 이동")
        events = ElevatorStateMachine.processCommand(events, ElevatorCommand.GoToFloor(10))

        println("시나리오 3: 이미 있는 층(10층) 버튼 누름")
        events = ElevatorStateMachine.processCommand(events, ElevatorCommand.GoToFloor(10))

        println("시나리오 4: 엘리베이터 고장.")
        events = events + ElevatorEvent.ElevatorBroken
        println("이벤트 추가: ElevatorBroken")
        println("현재 상태: ${ElevatorStateMachine.foldEvents(events)}")
        println("--------------------------------")

        println("시나리오 5: 고장난 엘리베이터 호출 시도")
        events = ElevatorStateMachine.processCommand(events, ElevatorCommand.CallElevator(3))

        println("시나리오 6: 엘리베이터 수리")
        events = ElevatorStateMachine.processCommand(events, ElevatorCommand.FixElevator)

        println("시나리오 7: 수리 후 엘리베이터 호출 (7층)")
        events = ElevatorStateMachine.processCommand(events, ElevatorCommand.CallElevator(7))

        println("===== 시뮬레이션 완료 =====")
    }
}

fun main() {
    val controller = ElevatorController()
    controller.execute()  // 결과 출력
}
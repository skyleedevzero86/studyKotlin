package com.functionstudy.ch06.statemachine

import com.functionstudy.ch06.command.ElevatorCommand
import com.functionstudy.ch06.event.ElevatorEvent
import com.functionstudy.ch06.state.ElevatorState

class ElevatorStateMachine {
    companion object {
        // 초기 상태 설정
        val initialState: ElevatorState = ElevatorState.DoorsOpenAtFloor(0)

        // 이벤트를 접어서(fold) 상태 계산
        fun foldEvents(events: List<ElevatorEvent>): ElevatorState {
            return events.fold(initialState) { state, event ->
                when (event) {
                    is ElevatorEvent.ButtonPressed -> state
                    is ElevatorEvent.ElevatorMoved -> ElevatorState.DoorsOpenAtFloor(event.toFloor)
                    is ElevatorEvent.ElevatorBroken -> ElevatorState.OutOfOrder
                    is ElevatorEvent.ElevatorFixed -> ElevatorState.DoorsOpenAtFloor(0)
                }
            }
        }

        // 명령 처리 함수 - 현재 상태와 명령을 받아 이벤트 리스트 반환
        fun handleCommand(state: ElevatorState, command: ElevatorCommand): List<ElevatorEvent> {
            return when (command) {
                is ElevatorCommand.CallElevator -> {
                    when (state) {
                        is ElevatorState.DoorsOpenAtFloor -> {
                            if (state.floor == command.floor) emptyList()
                            else listOf(
                                ElevatorEvent.ButtonPressed(command.floor),
                                ElevatorEvent.ElevatorMoved(state.floor, command.floor)
                            )
                        }
                        is ElevatorState.TravelingToFloor -> listOf(
                            ElevatorEvent.ButtonPressed(command.floor),
                            ElevatorEvent.ElevatorMoved(state.toFloor, command.floor)
                        )
                        is ElevatorState.OutOfOrder -> listOf(ElevatorEvent.ElevatorBroken)
                    }
                }
                is ElevatorCommand.GoToFloor -> {
                    when (state) {
                        is ElevatorState.DoorsOpenAtFloor -> {
                            if (state.floor == command.floor) emptyList()
                            else listOf(
                                ElevatorEvent.ButtonPressed(command.floor),
                                ElevatorEvent.ElevatorMoved(state.floor, command.floor)
                            )
                        }
                        is ElevatorState.TravelingToFloor -> emptyList()
                        is ElevatorState.OutOfOrder -> listOf(ElevatorEvent.ElevatorBroken)
                    }
                }
                is ElevatorCommand.FixElevator -> {
                    when (state) {
                        is ElevatorState.OutOfOrder -> listOf(ElevatorEvent.ElevatorFixed)
                        else -> emptyList()
                    }
                }
            }
        }

        // 이벤트 처리 및 상태 출력 함수
        fun processCommand(events: List<ElevatorEvent>, command: ElevatorCommand): List<ElevatorEvent> {
            val currentState = foldEvents(events)
            println("현재 상태: $currentState")

            val newEvents = handleCommand(currentState, command)
            println("명령: $command")

            if (newEvents.isEmpty()) println("변화 없음")
            else println("발생한 이벤트: $newEvents")

            val updatedEvents = events + newEvents
            val newState = foldEvents(updatedEvents)
            println("변경된 상태: $newState")
            println("--------------------------------")

            return updatedEvents
        }
    }
}

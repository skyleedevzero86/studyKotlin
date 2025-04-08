package com.functionstudy.onestest.ch06

import com.functionstudy.ones.ch06.command.ElevatorCommand
import com.functionstudy.ones.ch06.event.ElevatorEvent
import com.functionstudy.ones.ch06.state.ElevatorState
import com.functionstudy.ones.ch06.statemachine.ElevatorStateMachine
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@DisplayName("엘리베이터 상태 기계 테스트")
class ElevatorStateMachineTest {

    @Nested
    @DisplayName("기본 명령 테스트")
    inner class BasicCommandsTest {

        @Test
        @DisplayName("엘리베이터를 특정 층으로 호출하면 그 층에서 문이 열림")
        fun callElevatorToFloor() {
            // given
            val initialState = ElevatorState.DoorsOpenAtFloor(0)
            println("초기 상태: 엘리베이터가 0층에서 문이 열린 상태")

            // when
            val events = ElevatorStateMachine.handleCommand(initialState, ElevatorCommand.CallElevator(5))
            println("명령 실행: 5층으로 엘리베이터 호출")
            println("발생한 이벤트: $events")
            val finalState = ElevatorStateMachine.foldEvents(events)
            println("최종 상태: $finalState")

            // then
            expectThat(finalState).isEqualTo(ElevatorState.DoorsOpenAtFloor(5))
            println("테스트 성공: 엘리베이터가 5층에서 문이 열린 상태로 변경됨")
            println("---------------------------------")
        }

        @Test
        @DisplayName("엘리베이터 안에서 층 버튼을 누르면 해당 층으로 이동")
        fun pressFloorButtonInsideElevator() {
            // given
            val initialState = ElevatorState.DoorsOpenAtFloor(5)
            println("초기 상태: 엘리베이터가 5층에서 문이 열린 상태")

            // when
            val events = ElevatorStateMachine.handleCommand(initialState, ElevatorCommand.GoToFloor(10))
            println("명령 실행: 10층으로 이동 버튼 누름")
            println("발생한 이벤트: $events")
            val finalState = ElevatorStateMachine.foldEvents(events)
            println("최종 상태: $finalState")

            // then
            expectThat(finalState).isEqualTo(ElevatorState.DoorsOpenAtFloor(10))
            println("테스트 성공: 엘리베이터가 10층에서 문이 열린 상태로 변경됨")
            println("---------------------------------")
        }

        @Test
        @DisplayName("이미 있는 층 버튼을 누르면 상태 변화 없음")
        fun pressSameFloorButton() {
            // given
            val initialState = ElevatorState.DoorsOpenAtFloor(10)
            println("초기 상태: 엘리베이터가 10층에서 문이 열린 상태")

            // when
            val events = ElevatorStateMachine.handleCommand(initialState, ElevatorCommand.GoToFloor(10))
            println("명령 실행: 이미 10층에 있는 상태에서 10층 버튼 누름")
            println("발생한 이벤트: $events")
            val finalState = ElevatorStateMachine.foldEvents(events)
            println("최종 상태: $finalState")

            // then
            expectThat(finalState).isEqualTo(ElevatorState.DoorsOpenAtFloor(10))
            println("테스트 성공: 엘리베이터 상태 변화 없음")
            println("---------------------------------")
        }
    }

    @Nested
    @DisplayName("이벤트 처리 테스트")
    inner class EventProcessingTest {

        @Test
        @DisplayName("이벤트를 접어서 상태 계산")
        fun foldEventsTest() {
            // given
            val events = listOf(
                ElevatorEvent.ButtonPressed(5),
                ElevatorEvent.ElevatorMoved(0, 5)
            )
            println("주어진 이벤트: 5층 버튼이 눌리고, 엘리베이터가 0층에서 5층으로 이동")

            // when
            val finalState = ElevatorStateMachine.foldEvents(events)
            println("이벤트를 접어서 계산된 상태: $finalState")

            // then
            expectThat(finalState).isEqualTo(ElevatorState.DoorsOpenAtFloor(5))
            println("테스트 성공: 엘리베이터가 5층에서 문이 열린 상태로 계산됨")
            println("---------------------------------")
        }

        @Test
        @DisplayName("명령 실행으로 새 이벤트 생성 및 상태 업데이트")
        fun processCommandTest() {
            // given
            val events = listOf<ElevatorEvent>()
            println("초기 이벤트: 없음")

            // when
            val newEvents = ElevatorStateMachine.processCommand(events, ElevatorCommand.CallElevator(5))
            println("명령 실행: 5층으로 엘리베이터 호출")
            println("생성된 이벤트: $newEvents")
            val finalState = ElevatorStateMachine.foldEvents(newEvents)
            println("최종 상태: $finalState")

            // then
            expectThat(finalState).isEqualTo(ElevatorState.DoorsOpenAtFloor(5))
            println("테스트 성공: 엘리베이터가 5층에서 문이 열린 상태로 변경됨")
            println("---------------------------------")
        }
    }

    @Nested
    @DisplayName("고장 상태 테스트")
    inner class BrokenStateTest {

        @Test
        @DisplayName("엘리베이터가 고장나면 OutOfOrder 상태가 됨")
        fun elevatorBreaks() {
            // given
            val events = listOf(ElevatorEvent.ElevatorBroken)
            println("주어진 이벤트: 엘리베이터 고장")

            // when
            val state = ElevatorStateMachine.foldEvents(events)
            println("계산된 상태: $state")

            // then
            expectThat(state).isEqualTo(ElevatorState.OutOfOrder)
            println("테스트 성공: 엘리베이터가 고장 상태로 변경됨")
            println("---------------------------------")
        }

        @Test
        @DisplayName("고장난 엘리베이터에 호출 명령을 보내면 상태 변화 없음")
        fun callBrokenElevator() {
            // given
            val initialState = ElevatorState.OutOfOrder
            println("초기 상태: 엘리베이터 고장")

            // when
            val events = ElevatorStateMachine.handleCommand(initialState, ElevatorCommand.CallElevator(5))
            println("명령 실행: 고장난 엘리베이터를 5층으로 호출")
            println("발생한 이벤트: $events")
            val finalState = ElevatorStateMachine.foldEvents(events)
            println("최종 상태: $finalState")

            // then
            expectThat(finalState).isEqualTo(ElevatorState.OutOfOrder)
            println("테스트 성공: 엘리베이터가 계속 고장 상태로 유지됨")
            println("---------------------------------")
        }

        @Test
        @DisplayName("고장난 엘리베이터 수리 명령으로 상태 복구")
        fun fixBrokenElevator() {
            // given
            val initialState = ElevatorState.OutOfOrder
            println("초기 상태: 엘리베이터 고장")

            // when
            val events = ElevatorStateMachine.handleCommand(initialState, ElevatorCommand.FixElevator)
            println("명령 실행: 고장난 엘리베이터 수리")
            println("발생한 이벤트: $events")
            val finalState = ElevatorStateMachine.foldEvents(events)
            println("최종 상태: $finalState")

            // then
            expectThat(finalState).isEqualTo(ElevatorState.DoorsOpenAtFloor(0))
            println("테스트 성공: 엘리베이터가 0층에서 문이 열린 상태로 복구됨")
            println("---------------------------------")
        }

        @Test
        @DisplayName("정상 상태 엘리베이터에 수리 명령을 보내면 상태 변화 없음")
        fun fixWorkingElevator() {
            // given
            val initialState = ElevatorState.DoorsOpenAtFloor(5)
            println("초기 상태: 엘리베이터가 5층에서 문이 열린 상태")

            // when
            val events = ElevatorStateMachine.handleCommand(initialState, ElevatorCommand.FixElevator)
            println("명령 실행: 정상 상태의 엘리베이터 수리 시도")
            println("발생한 이벤트: $events")
            val finalState = ElevatorStateMachine.foldEvents(events)
            println("최종 상태: $finalState")

            // then
            expectThat(finalState).isEqualTo(ElevatorState.DoorsOpenAtFloor(5))
            println("테스트 성공: 엘리베이터 상태 변화 없음")
            println("---------------------------------")
        }
    }
}
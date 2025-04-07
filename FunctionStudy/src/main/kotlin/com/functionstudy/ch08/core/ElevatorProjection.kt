package com.functionstudy.ch08.core

data class ElevatorProjectionRow(val elevatorId: Int, val floor: Int, val state: ElevatorState)

fun foldEvents(events: List<ElevatorEvent>): ElevatorState =
    events.fold(ElevatorState.DoorsOpenAtFloor(0) as ElevatorState) { state, event ->
        when (event) {
            is ElevatorEvent.ButtonPressed ->
                if (state != ElevatorState.DoorsOpenAtFloor(event.floor)) ElevatorState.TravelingAtFloor(event.floor)
                else state
            is ElevatorEvent.ElevatorMoved -> ElevatorState.DoorsOpenAtFloor(event.toFloor)
            is ElevatorEvent.ElevatorBroken -> ElevatorState.OutOfOrder
            is ElevatorEvent.ElevatorFixed -> ElevatorState.DoorsOpenAtFloor(0) // 엘리베이터가 고쳐지면 지상층으로 돌아간다고 가정
        }
    }

interface ElevatorProjection {
    fun allRows(): List<ElevatorProjectionRow>
    fun getRow(elevatorId: Int): ElevatorProjectionRow?
}

class ElevatorProjectionInMemory(private val events: List<ElevatorEvent>) : ElevatorProjection {
    private val projectionRows: List<ElevatorProjectionRow> = events
        .groupBy { it.elevatorId }
        .mapValues { (_, events) ->
            val finalState = foldEvents(events)
            ElevatorProjectionRow(events.first().elevatorId, finalState.floor, finalState)
        }
        .values.toList()

    override fun allRows(): List<ElevatorProjectionRow> = projectionRows

    override fun getRow(elevatorId: Int): ElevatorProjectionRow? =
        projectionRows.find { it.elevatorId == elevatorId }
}

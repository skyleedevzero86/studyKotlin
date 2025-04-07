package com.functionstudy.ch08.core

sealed class ElevatorEvent {
    abstract val elevatorId: Int
    data class ButtonPressed(override val elevatorId: Int, val floor: Int) : ElevatorEvent()
    data class ElevatorMoved(override val elevatorId: Int, val fromFloor: Int, val toFloor: Int) : ElevatorEvent()
    data class ElevatorBroken(override val elevatorId: Int) : ElevatorEvent()
    data class ElevatorFixed(override val elevatorId: Int) : ElevatorEvent()
}
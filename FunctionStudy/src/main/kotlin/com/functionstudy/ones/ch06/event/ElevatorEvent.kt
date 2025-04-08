package com.functionstudy.ones.ch06.event

sealed class ElevatorEvent {
    data class ButtonPressed(val floor: Int) : ElevatorEvent()
    data class ElevatorMoved(val fromFloor: Int, val toFloor: Int) : ElevatorEvent()
    object ElevatorBroken : ElevatorEvent()
    object ElevatorFixed : ElevatorEvent()
}
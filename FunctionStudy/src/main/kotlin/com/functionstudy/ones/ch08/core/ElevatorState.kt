package com.functionstudy.ones.ch08.core

sealed class ElevatorState {
    abstract val floor: Int
    data class DoorsOpenAtFloor(override val floor: Int) : ElevatorState()
    data class TravelingAtFloor(override val floor: Int) : ElevatorState()
    object OutOfOrder : ElevatorState() {
        override val floor: Int = 0
    }
}
package com.functionstudy.ones.ch06.state

sealed class ElevatorState {
    data class DoorsOpenAtFloor(val floor: Int) : ElevatorState()
    data class TravelingToFloor(val fromFloor: Int, val toFloor: Int) : ElevatorState()
    object OutOfOrder : ElevatorState()
}
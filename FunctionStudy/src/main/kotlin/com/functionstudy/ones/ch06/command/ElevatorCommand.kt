package com.functionstudy.ones.ch06.command

sealed class ElevatorCommand {
    data class CallElevator(val floor: Int) : ElevatorCommand()
    data class GoToFloor(val floor: Int) : ElevatorCommand()
    object FixElevator : ElevatorCommand()
}
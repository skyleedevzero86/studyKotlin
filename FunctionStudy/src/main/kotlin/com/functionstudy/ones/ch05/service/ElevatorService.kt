package com.functionstudy.ones.ch05.service

import com.functionstudy.ch05.domain.Direction
import com.functionstudy.ch05.domain.Down
import com.functionstudy.ch05.domain.Elevator
import com.functionstudy.ch05.domain.Up

class ElevatorService {
    fun calculateFinalFloor(directions: List<Direction>): Elevator {
        return directions.fold(Elevator(0)) { elevator, direction ->
            when (direction) {
                is Up -> Elevator(elevator.floor + 1)
                is Down -> Elevator(elevator.floor - 1)
            }
        }
    }
}
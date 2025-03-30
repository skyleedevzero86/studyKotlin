package com.functionstudy.ch05.controller

import com.functionstudy.ch05.domain.Down
import com.functionstudy.ch05.domain.Up
import com.functionstudy.ch05.service.ElevatorService

class  ElevatorController{
    fun execute() {
        val values = listOf(Up, Up, Down, Up, Down, Down, Up, Up, Up, Down)
        val elevatorService = ElevatorService()
        val tot = elevatorService.calculateFinalFloor(values)
        println("Final elevator floor: ${tot.floor}")
    }
}

fun main() {
    val controller = ElevatorController()
    controller.execute()  // 결과 출력
}
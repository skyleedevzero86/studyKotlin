package com.functionstudy.ones.ch10.controller

import com.functionstudy.ones.ch10.core.ConsoleRpnCalculator
import com.functionstudy.ones.ch10.core.SystemConsole

class RpnNewController {
    fun execute() {
        ConsoleRpnCalculator().runWith(SystemConsole())
    }
}

fun main() {
    val controller = RpnNewController()
    controller.execute()  // 결과 출력
}
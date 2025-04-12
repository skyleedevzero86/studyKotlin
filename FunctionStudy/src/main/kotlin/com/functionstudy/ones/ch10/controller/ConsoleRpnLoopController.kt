package com.functionstudy.ones.ch10.controller

import com.functionstudy.ones.ch10.core.*


class ConsoleRpnLoopController {
    fun execute() {
        consoleRpnLoop().runWith(SystemConsole())
    }
}

fun main() {
    val controller = ConsoleRpnLoopController()
    controller.execute()  // 결과 출력
}
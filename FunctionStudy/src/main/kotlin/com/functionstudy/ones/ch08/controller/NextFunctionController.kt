package com.functionstudy.ones.ch08.controller

import com.functionstudy.ones.ch08.domain.NextFunction

class NextFunctionController {
    fun execute() {
        val names = listOf("Ann", "Bob", "Charlie", "Dorothy")
        val nextName = NextFunction(names)

        println(nextName()) // "Ann"
        println(nextName()) // "Bob"
        println(nextName()) // "Charlie"
        println(nextName()) // "Dorothy"
        println(nextName()) // null
    }
}

fun main() {
    val controller = NextFunctionController()
    controller.execute()  // 결과 출력
}
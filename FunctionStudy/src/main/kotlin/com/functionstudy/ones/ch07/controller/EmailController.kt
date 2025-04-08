package com.functionstudy.ones.ch07.controller

import com.functionstudy.ch07.domain.sendEmail

class EmailController {
    fun execute() {
        // 성공 케이스
        val successResult = sendEmail("normal.txt")
        println("성공 결과: $successResult")

        // 실패 케이스
        val failureResult = sendEmail("errorfile.txt")
        println("실패 결과: $failureResult")
    }
}

fun main() {
    val controller = EmailController()
    controller.execute()  // 결과 출력
}
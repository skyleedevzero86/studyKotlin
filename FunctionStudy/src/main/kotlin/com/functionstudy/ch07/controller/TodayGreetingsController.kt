package com.functionstudy.ch07.controller

import com.functionstudy.ch07.domain.recover
import com.functionstudy.ch07.domain.tryAndCatch
import java.time.LocalDate

class TodayGreetingsController {
    fun execute() {
        val successResult = todayGreetings("2024-09-12")
        println(successResult)

        // 실패 케이스
        val failureResult = todayGreetings("12/09/2024")
        println(failureResult)
    }

    fun todayGreetings(dateString: String): String =
        tryAndCatch { LocalDate.parse(dateString) }
            .transform { "Today is $it" }
            .recover { "날짜 형식이 올바르지 않습니다: ${it.msg}" }
}

fun main() {
    val controller = TodayGreetingsController()
    controller.execute()  // 결과 출력
}

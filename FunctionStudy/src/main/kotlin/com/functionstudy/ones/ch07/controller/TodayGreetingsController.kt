package com.functionstudy.ones.ch07.controller

import java.time.LocalDate
import com.functionstudy.ones.ch07.domain.fold
import com.functionstudy.ones.ch07.domain.tryAndCatch

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
            .fold(
                success = { parsedDate -> "Today is $parsedDate" },
                failure = { error -> "날짜 형식이 올바르지 않습니다: ${error.msg}" }
            )
}

fun main() {
    val controller = TodayGreetingsController()
    controller.execute()  // 결과 출력
}

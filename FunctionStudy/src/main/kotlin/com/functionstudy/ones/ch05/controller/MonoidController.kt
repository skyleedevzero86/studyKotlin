package com.functionstudy.ones.ch05.controller

import com.functionstudy.ones.ch05.domain.Money
import com.functionstudy.ones.ch05.domain.Monoid
import com.functionstudy.ones.ch05.domain.zeroMoney

class MonoidController {
    fun execute() {

        with(Monoid(0, Int::plus)) {
            val result = listOf(1, 2, 3, 4, 10).fold()
            println("정수 모노이드 결과: $result")
        }


        with(Monoid("", String::plus)) {
            val result = listOf("My", "Fair", "Lady").fold()
            println("문자열 모노이드 결과: $result")
        }

        with(Monoid(zeroMoney, Money::sum)) {
            val result = listOf(
                Money(2.1),
                Money(3.9),
                Money(4.0)
            ).fold()
            println("금액 모노이드 결과: $result")
        }
    }
}
fun main() {
    val controller = MonoidController()
    controller.execute()  // 결과 출력
}
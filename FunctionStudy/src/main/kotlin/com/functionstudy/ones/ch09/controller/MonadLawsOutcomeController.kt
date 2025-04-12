package com.functionstudy.ones.ch09.controller

import com.functionstudy.ones.ch09.core.result.Outcome

class MonadLawsOutcomeController {
    fun execute() {
        val value = 7

        // 값을 받아 3을 더한 후 Outcome.Success로 반환
        fun add3(x: Int): Outcome<Int> = Outcome.Success(x + 3)

        println("값 $value 를 포함한 Outcome 생성")
        val left = Outcome.Success(value).flatMap(::add3)
        println("왼쪽 flatMap 결과: $left")

        val right = add3(value)
        println("오른쪽 add3 결과: $right")

        if (left == right) {
            println("왼쪽과 오른쪽 결과가 같음: $left == $right")
        } else {
            println("결과 불일치: 왼쪽은 $left, 오른쪽은 $right")
        }

        println("값 $value 를 포함한 Outcome 생성")
        val outcome = Outcome.Success(value)
        println("Outcome 생성: $outcome")

        val leftIdentity = outcome.flatMap { Outcome.Success(it) }
        println("왼쪽 flatMap 결과: $leftIdentity")

        if (leftIdentity == outcome) {
            println("왼쪽 flatMap 결과와 원본 Outcome이 같음: $leftIdentity == $outcome")
        } else {
            println("결과 불일치: 왼쪽은 $leftIdentity, 원본은 $outcome")
        }

        println("값 $value 를 포함한 Outcome 생성")

        val f: (Int) -> Outcome<Int> = { Outcome.Success(it * 2) }
        val g: (Int) -> Outcome<Int> = { Outcome.Success(it + 10) }

        val leftAssoc = Outcome.Success(value).flatMap(f).flatMap(g)
        println("왼쪽 flatMap(f) 후 flatMap(g) 결과: $leftAssoc")

        val rightAssoc = Outcome.Success(value).flatMap { x -> f(x).flatMap(g) }
        println("오른쪽 flatMap(f) 후 flatMap(g) 결과: $rightAssoc")

        if (leftAssoc == rightAssoc) {
            println("왼쪽과 오른쪽 결과가 같음: $leftAssoc == $rightAssoc")
        } else {
            println("결과 불일치: 왼쪽은 $leftAssoc, 오른쪽은 $rightAssoc")
        }
    }
}
fun main() {
    val controller = MonadLawsOutcomeController()
    controller.execute()  // 결과 출력
}
package com.functionstudy.ones.ch09.controller

class MonadLawsLisController {
    fun execute() {
        val value = 5

        // 값 x에 대해 두 배의 값을 반환하는 함수
        fun double(x: Int): List<Int> = listOf(x, x * 2)

        // Left
        println("값 $value 를 포함한 리스트 생성")
        val left = listOf(value).flatMap(::double)
        println("왼쪽 flatMap 결과: $left")

        val right = double(value)
        println("오른쪽 double 결과: $right")

        if (left == right) {
            println("왼쪽과 오른쪽 결과가 같음: $left == $right")
        } else {
            println("결과 불일치: 왼쪽은 $left, 오른쪽은 $right")
        }

        // Right
        val lst = listOf(1, 2, 3)
        println("리스트 $lst 생성")

        val leftIdentity = lst.flatMap { listOf(it) }
        println("왼쪽 flatMap 결과: $leftIdentity")

        if (leftIdentity == lst) {
            println("왼쪽 flatMap 결과와 원본 리스트가 같음: $leftIdentity == $lst")
        } else {
            println("결과 불일치: 왼쪽은 $leftIdentity, 원본은 $lst")
        }

        val f: (Int) -> List<Int> = { listOf(it, it + 1) }
        val g: (Int) -> List<Int> = { listOf(it * 2) }

        val list = listOf(value)
        println("값 $value 를 포함한 리스트 생성")

        val leftAssoc = list.flatMap(f).flatMap(g)
        println("왼쪽 flatMap(f) 후 flatMap(g) 결과: $leftAssoc")

        val rightAssoc = list.flatMap { x -> f(x).flatMap(g) }
        println("오른쪽 flatMap(f) 후 flatMap(g) 결과: $rightAssoc")

        if (leftAssoc == rightAssoc) {
            println("왼쪽과 오른쪽 결과가 같음: $leftAssoc == $rightAssoc")
        } else {
            println("결과 불일치: 왼쪽은 $leftAssoc, 오른쪽은 $rightAssoc")
        }
    }
}

fun main() {
    val controller = MonadLawsLisController()
    controller.execute()  // 결과 출력
}
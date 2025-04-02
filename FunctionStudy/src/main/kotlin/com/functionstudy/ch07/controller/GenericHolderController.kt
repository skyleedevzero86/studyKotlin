package com.functionstudy.ch07.controller

import com.functionstudy.ch07.domain.Holder

class GenericHolderController {
    fun execute() {
        val h = Holder("hello")
        val w = Holder("world")
        val result = h.combine(w) { a, b -> "$a $b" }
        println(result)
    }
}

fun <T> Holder<T>.combine(other: Holder<T>, f: (T, T) -> T): Holder<T> =
    this.transform { myValue -> f(myValue, other.value) }

fun main() {
    val controller = GenericHolderController()
    controller.execute()  // 결과 출력
}
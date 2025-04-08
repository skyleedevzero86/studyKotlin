package com.functionstudy.ones.ch04.controller

import com.functionstudy.ch04.curry.curry
import com.functionstudy.ch04.curry.plusPlus
class CurryingExtensionsController {
    fun execute() {

        val sum: (Int, Int) -> Int = { num1, num2 -> num1 + num2 }
        val strConcat: (String, String) -> String = { s1, s2 -> "$s1$s2" }


        val plus3Fn = sum.curry()(3)
        val starPrefixFn = strConcat.curry()("*")

        val curriedConcat = strConcat.curry()
        val curriedSum = sum.curry()


        println("plus3Fn(4): ${plus3Fn(4)}")
        println("starPrefixFn(\"abc\"): ${starPrefixFn("abc")}")

        println("curriedConcat +++ \"head\" +++ \"tail\": ${curriedConcat.plusPlus("head")("tail")}")
        println("curriedSum +++ 4 +++ 5: ${curriedSum.plusPlus(4)(5)}")
    }
}

fun main() {
    val controller = CurryingExtensionsController()
    controller.execute()
}

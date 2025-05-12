package com.functionstudy.ones.ch011.controller

import com.functionstudy.ones.ch10.core.*

class ContextReaderExtensionsController {
    fun execute() {
        val sum3: (Int, Int, Int) -> Int = { a, b, c -> a + b + c }

        data class MyContext(val x: Int, val y: Int, val z: Int)

        val readX = ContextReader<MyContext, Int> { it.x }
        val readY = ContextReader<MyContext, Int> { it.y }
        val readZ = ContextReader<MyContext, Int> { it.z }

        val curriedSum3: ContextReader<MyContext, (Int) -> (Int) -> (Int) -> Int> =
            ContextReader { ctx -> sum3.curried() }

        val resultReader: ContextReader<MyContext, Int> =
            curriedSum3 * readX * readY * readZ

        val result = resultReader.runWith(MyContext(10, 20, 30))
        println("Result: $result")  // Expected output: Result: 60
    }
}

fun main() {
    val controller = ContextReaderExtensionsController()
    controller.execute()
}
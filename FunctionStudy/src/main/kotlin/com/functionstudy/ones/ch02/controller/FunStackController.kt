package com.functionstudy.ones.ch02.controller


import com.functionstudy.ch02.stack.FunStack

class FunStackController {

    fun execute() {
        val stack = FunStack<Char>()

        stack.push('A')
        stack.push('B')

        println("스택사이즈: ${stack.size()}")

        val poppedElement = stack.pop()
        println("팝의 요소: $poppedElement")

        println("팝에서 후 스택으로 처리된 크기 : ${stack.size()}")
    }

}

 fun main() {
   val controller = FunStackController()
        controller.execute()
  }
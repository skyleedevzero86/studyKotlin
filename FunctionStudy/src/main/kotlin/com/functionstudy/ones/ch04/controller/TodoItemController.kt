package com.functionstudy.ones.ch04.controller

import com.functionstudy.ch04.extensions.discardUnless  // 확장 함수 import 추가
import com.functionstudy.ch04.item.TodoItem
import com.functionstudy.ch04.item.TodoStatus

class TodoItemController {
    fun execute() {
        val itemInProgress = TodoItem("doing something", TodoStatus.InProgress)
        val itemBlocked = TodoItem("must do something", TodoStatus.Blocked)

        val resultInProgress = itemInProgress.discardUnless { it.status == TodoStatus.InProgress }
        val resultBlocked = itemBlocked.discardUnless { it.status == TodoStatus.InProgress }

        println("InProgress 결과: $resultInProgress")
        println("Blocked 결과: $resultBlocked")
    }
}

fun main() {
    val controller = TodoItemController()
    controller.execute()
}

package com.functionstudy.ones.ch011.controller

import com.functionstudy.ones.ch10.core.*
import java.util.UUID
import com.functionstudy.ones.ch07.domain.Outcome
import com.functionstudy.ones.ch011.core.*
import com.functionstudy.ones.ch07.inter.SimpleOutcomeError
import java.time.Instant

class ToDoItemDeletedController {
    fun execute() {
        // 테스트용 UUID 생성
        val listId = UUID.randomUUID()
        val itemIdToDelete = UUID.randomUUID()
        val anotherItemId = UUID.randomUUID()

        // 샘플 ToDoList 생성
        val item1 = ToDoItem(id = itemIdToDelete, text = "할 일 1", status = ItemStatus.PENDING)
        val item2 = ToDoItem(id = anotherItemId, text = "할 일 2", status = ItemStatus.PENDING)
        val toDoList = ToDoList(id = listId, items = listOf(item1, item2))

        val lists = mapOf(listId to toDoList)

        // 삭제 명령 생성
        val command = DeleteToDoItem(listId = listId, itemId = itemIdToDelete)

        // 함수 실행
        val result = handleDeleteToDoItem(command, lists)

        // 결과 출력
        when (result) {
            is Outcome.Success -> {
                val (event, updatedLists) = result.value
                println("이벤트 발생: $event")
                println("업데이트된 목록:")
                updatedLists.forEach { (id, list) ->
                    println("목록 ID: $id")
                    list.items.forEach { println(" - ${it.text}") }
                }
            }
            is Outcome.Failure -> {
                println("실패: ${result.error}")
            }
        }
    }
}

fun handleDeleteToDoItem(
    command: DeleteToDoItem,
    lists: Map<UUID, ToDoList>
): Outcome<*, Pair<DeleteToDoItemEvent, Map<UUID, ToDoList>>> {
    val list = lists[command.listId] ?: return Outcome.Failure(SimpleOutcomeError("항목을 찾을수가 없습니다."))
    val updatedItems = list.items.filterNot { it.id == command.itemId }
    val updatedList = list.copy(items = updatedItems)
    val updatedLists = lists + (command.listId to updatedList)

    val event = DeleteToDoItemEvent(
        listId = command.listId,
        itemId = command.itemId,
        timestamp = Instant.now()
    )

    return Outcome.Success(event to updatedLists)
}

fun main() {
    val controller = ToDoItemDeletedController()
    controller.execute()
}

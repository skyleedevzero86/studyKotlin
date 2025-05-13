package com.functionstudy.onestest.ch011

import com.functionstudy.ones.ch011.core.*
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import strikt.api.expectThat
import strikt.assertions.*
import com.functionstudy.ones.ch07.domain.Outcome
import com.functionstudy.ones.ch07.domain.Outcome.Success
import com.functionstudy.ones.ch07.domain.Outcome.Failure
import java.time.Instant

class DeleteToDoItemTest {
    private lateinit var lists: Map<UUID, ToDoList>
    private lateinit var listId: UUID
    private lateinit var itemId1: UUID
    private lateinit var itemId2: UUID

    // 반환 타입을 수정하여 정확한 제네릭을 사용하도록 함
    fun handleDeleteToDoItem(
        command: DeleteToDoItem,
        lists: Map<UUID, ToDoList>
    ): Outcome<DomainError, Pair<ToDoItemDeleted, Map<UUID, ToDoList>>> {
        val list = lists[command.listId] ?: return Outcome.Failure(ListNotFound(command.listId))
        val itemToDelete = list.items.find { it.id == command.itemId }
            ?: return Outcome.Failure(ItemNotFound(command.itemId))

        val updatedItems = list.items.filterNot { it.id == command.itemId }
        val updatedList = list.copy(items = updatedItems)
        val updatedLists = lists + (command.listId to updatedList)

        val event = ToDoItemDeleted(
            listId = command.listId,
            itemId = command.itemId,
            timestamp = Instant.now()
        )

        return Outcome.Success(event to updatedLists)
    }


    @BeforeEach
    fun setup() {
        itemId1 = UUID.randomUUID()
        itemId2 = UUID.randomUUID()
        listId = UUID.randomUUID()

        val item1 = ToDoItem(itemId1, "우유 사기", ItemStatus.TODO)
        val item2 = ToDoItem(itemId2, "빵 사기", ItemStatus.TODO)
        val list = ToDoList(listId, listOf(item1, item2))

        lists = mapOf(listId to list)
    }

    @Test
    @DisplayName("항목 삭제 명령이 성공적으로 처리되어야 한다")
    fun itemDeleteSuccess() {
        // given
        val command = DeleteToDoItem(listId, itemId1)

        // when
        val result = handleDeleteToDoItem(command, lists)

        // then
        println("Test: 항목 삭제 명령이 성공적으로 처리되어야 한다")
        expectThat(result).isA<Success<Pair<ToDoItemDeleted, Map<UUID, ToDoList>>>>() // 성공 타입 변경

        val (event, updatedLists) = (result as Success<Pair<ToDoItemDeleted, Map<UUID, ToDoList>>>).value

        println("Deleted Event: $event")
        expectThat(event).isA<ToDoItemDeleted>()
        val deleteEvent = event as ToDoItemDeleted
        expectThat(deleteEvent.listId).isEqualTo(listId)
        expectThat(deleteEvent.itemId).isEqualTo(itemId1)

        val updatedList = updatedLists[listId]!!
        println("Updated List: $updatedList")
        expectThat(updatedList.items.map { it.id }).doesNotContain(itemId1)
        expectThat(updatedList.items.size).isEqualTo(1)
        expectThat(updatedList.items[0].id).isEqualTo(itemId2)
    }

    @Test
    @DisplayName("존재하지 않는 목록에 대한 삭제 명령은 실패해야 한다")
    fun nonExistingListDeleteFail() {
        // given
        val nonExistingListId = UUID.randomUUID()
        val command = DeleteToDoItem(nonExistingListId, itemId1)

        // when
        val result = handleDeleteToDoItem(command, lists)

        // then
        println("Test: 존재하지 않는 목록에 대한 삭제 명령은 실패해야 한다")
        expectThat(result).isA<Failure<DomainError>>()
        val error = (result as Failure<DomainError>).error
        println("Error: $error")
        expectThat(error).isA<ListNotFound>()
        expectThat((error as ListNotFound).listId).isEqualTo(nonExistingListId)
    }

    @Test
    @DisplayName("존재하지 않는 항목에 대한 삭제 명령은 실패해야 한다")
    fun nonExistingItemDeleteFail() {
        // given
        val nonExistingItemId = UUID.randomUUID()
        val command = DeleteToDoItem(listId, nonExistingItemId)

        // when
        val result = handleDeleteToDoItem(command, lists)

        // then
        println("Test: 존재하지 않는 항목에 대한 삭제 명령은 실패해야 한다")
        expectThat(result).isA<Failure<DomainError>>()
        val error = (result as Failure<DomainError>).error
        println("Error: $error")
        expectThat(error).isA<ItemNotFound>()
        expectThat((error as ItemNotFound).itemId).isEqualTo(nonExistingItemId)
    }
}

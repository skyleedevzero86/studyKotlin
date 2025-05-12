package com.functionstudy.onestest.ch011

import com.functionstudy.ones.ch011.core.ItemStatus
import com.functionstudy.ones.ch011.core.ToDoItem
import com.functionstudy.ones.ch07.domain.Outcome
import com.functionstudy.ones.ch07.domain.bind
import com.functionstudy.ones.ch07.inter.OutcomeError
import com.functionstudy.ones.ch07.inter.SimpleOutcomeError
import com.functionstudy.ones.ch07.inter.asFailure
import com.functionstudy.ones.ch07.inter.asSuccess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

class EditToDoItemTest {

    private fun validateText(text: String): Outcome<OutcomeError, String> =
        if (text.isNotBlank()) text.asSuccess()
        else SimpleOutcomeError("텍스트는 비워둘 수 없습니다").asFailure()

    private fun updateStatus(item: ToDoItem, newStatus: ItemStatus): Outcome<OutcomeError, ToDoItem> =
        if (item.status != newStatus) item.copy(status = newStatus).asSuccess()
        else SimpleOutcomeError("이미 상태가 ${newStatus.name}입니다").asFailure()

    private fun saveItem(item: ToDoItem): Outcome<OutcomeError, ToDoItem> =
        item.asSuccess()

    private fun 편집하기(item: ToDoItem, newText: String, newStatus: ItemStatus): Outcome<OutcomeError, ToDoItem> {
        return validateText(newText)
            .bind { validatedText ->
                updateStatus(item, newStatus)
                    .bind { updatedItem ->
                        saveItem(updatedItem.copy(text = validatedText))
                    }
            }
    }

    private fun Outcome<OutcomeError, ToDoItem>.한글결과출력() {
        when (this) {
            is Outcome.Success -> println("성공: ${this.value}")
            is Outcome.Failure -> println("실패: ${this.error.msg}")
        }
    }

    @Test
    @DisplayName("할 일 항목의 텍스트와 상태를 성공적으로 변경한다")
    fun `텍스트와 상태 변경 성공`() {
        // GIVEN
        val item = ToDoItem(UUID.randomUUID(), "Old text", ItemStatus.TODO)
        val newText = "New text"
        val newStatus = ItemStatus.DONE

        // WHEN
        val result = 편집하기(item, newText, newStatus)

        // THEN
        result.한글결과출력()
        assertEquals(Outcome.Success(item.copy(text = newText, status = newStatus)), result)
    }

    @Test
    @DisplayName("텍스트가 공백일 경우 실패한다")
    fun `텍스트 공백 실패`() {
        // GIVEN
        val item = ToDoItem(UUID.randomUUID(), "Old text", ItemStatus.TODO)
        val newText = ""
        val newStatus = ItemStatus.DONE

        // WHEN
        val result = 편집하기(item, newText, newStatus)

        // THEN
        result.한글결과출력()
        assertEquals(Outcome.Failure(SimpleOutcomeError("텍스트는 비워둘 수 없습니다")), result)
    }

    @Test
    @DisplayName("상태가 변경되지 않았을 경우 실패한다")
    fun `상태 변경 없음 실패`() {
        // GIVEN
        val item = ToDoItem(UUID.randomUUID(), "Old text", ItemStatus.TODO)
        val newText = "New text"
        val newStatus = ItemStatus.TODO

        // WHEN
        val result = 편집하기(item, newText, newStatus)

        // THEN
        result.한글결과출력()
        assertEquals(Outcome.Failure(SimpleOutcomeError("이미 상태가 TODO입니다")), result)
    }
}

package com.functionstudy.onestest.ch04

import com.functionstudy.ones.ch04.item.TodoItem
import com.functionstudy.ones.ch04.item.TodoStatus
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import com.functionstudy.ones.ch04.extensions.discardUnless

class DiscardUnlessTest {

    @Test
    fun testDiscardUnless() {
        // give: 테스트에 필요한 TodoItem 객체 준비
        val itemInProgress = TodoItem("doing something", TodoStatus.InProgress)
        val itemBlocked = TodoItem("must do something", TodoStatus.Blocked)

        // when: discardUnless 확장 함수 호출
        val resultInProgress = itemInProgress.discardUnless { it.status == TodoStatus.InProgress }
        val resultBlocked = itemBlocked.discardUnless { it.status == TodoStatus.InProgress }

        // then: 결과가 예상한 값과 일치하는지 검증
        println("InProgress 결과: $resultInProgress")
        println("Blocked 결과: $resultBlocked")
        assertEquals(itemInProgress, resultInProgress)
        assertNull(resultBlocked)
    }
}

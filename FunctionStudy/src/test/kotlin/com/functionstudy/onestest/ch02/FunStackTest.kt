package com.functionstudy.onestest.ch02

import com.functionstudy.ch02.stack.FunStack
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FunStackTest {

    // Test case 1: 스택에 요소 푸시
    @Test
    fun `push into the stack`() {
        // Given: 빈 스택 생성
        val stack1 = FunStack<Char>()

        // When: 스택에 'A' 푸시
        val stack2 = stack1.apply { push('A') }

        // Then: 스택1 크기는 0, 스택2 크기는 1이어야 한다.
        assertEquals(0, stack1.size())
        assertEquals(1, stack2.size())
    }

    // Test case 2: 푸시하고 팝
    @Test
    fun `push push pop`() {
        // Given: 빈 스택 생성
        val stack = FunStack<Char>().apply {
            push('A')  // 'A' 푸시
            push('B')  // 'B' 푸시
        }

        // When: 스택에서 팝
        val poppedElement = stack.pop()

        // Then: 팝된 요소는 'B'이어야 하고, 스택 크기는 1이어야 한다.
        assertEquals(1, stack.size())
        assertEquals('B', poppedElement)
    }
}

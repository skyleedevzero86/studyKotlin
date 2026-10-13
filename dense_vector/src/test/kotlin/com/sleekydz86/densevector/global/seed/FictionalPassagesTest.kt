package com.sleekydz86.densevector.global.seed

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FictionalPassagesTest {
    @Test
    fun titleFitsUniqueColumn() {
        val title = FictionalPassages.title(1000)
        assertEquals("가상 문단 1000", title)
        assertTrue(title.length <= 200)
    }

    @Test
    fun bodyMentionsSequenceAndStaysFictional() {
        val body = FictionalPassages.body(2)
        assertTrue(body.contains("가상 문단 2"))
        assertTrue(body.contains("운영 데이터가 아니다"))
    }

    @Test
    fun oneThousandTitlesStayUnique() {
        val titles = (1..1000).map { FictionalPassages.title(it) }
        assertEquals(1000, titles.toSet().size)
        assertTrue(titles.all { it.length <= 200 })
    }
}

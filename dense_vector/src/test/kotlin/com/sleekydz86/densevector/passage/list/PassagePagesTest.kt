package com.sleekydz86.densevector.passage.list

import com.sleekydz86.densevector.global.error.VectorStudyException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PassagePagesTest {
    @Test
    fun defaultsToFirstPageOfTwenty() {
        assertEquals(0 to 20, PassagePages.normalize(null, null))
    }

    @Test
    fun rejectsNegativePage() {
        assertFailsWith<VectorStudyException> { PassagePages.normalize(-1, 20) }
    }

    @Test
    fun rejectsSizeAboveLimit() {
        assertFailsWith<VectorStudyException> { PassagePages.normalize(0, 101) }
    }
}

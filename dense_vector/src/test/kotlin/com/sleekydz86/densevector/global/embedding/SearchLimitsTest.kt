package com.sleekydz86.densevector.global.embedding

import com.sleekydz86.densevector.global.error.VectorStudyException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SearchLimitsTest {
    @Test
    fun missingKDefaultsToFive() {
        assertEquals(5, SearchLimits.normalizeK(null))
    }

    @Test
    fun kAboveLimitIsRejected() {
        assertFailsWith<VectorStudyException> { SearchLimits.normalizeK(21) }
    }

    @Test
    fun candidatesAreAtLeastK() {
        assertEquals(20, SearchLimits.numCandidates(20, 10))
        assertEquals(100, SearchLimits.numCandidates(5, 100))
    }
}

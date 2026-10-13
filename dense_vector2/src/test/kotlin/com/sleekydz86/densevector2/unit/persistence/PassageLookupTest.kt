package com.sleekydz86.densevector2.unit.persistence

import com.sleekydz86.densevector2.global.embedding.HashingEmbedder
import com.sleekydz86.densevector2.global.error.VectorStudyException
import com.sleekydz86.densevector2.global.passage.PassageViews
import com.sleekydz86.densevector2.global.persistence.PassageLookup
import com.sleekydz86.densevector2.support.InMemoryPassages
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PassageLookupTest {
    @Test
    fun missingIdNamesTheRequestedValue() {
        val error = assertFailsWith<VectorStudyException> {
            PassageLookup(InMemoryPassages().repository).get(7)
        }

        assertTrue(error.message!!.contains("id=7"))
    }

    @Test
    fun getReturnsTheStoredRow() {
        val passages = InMemoryPassages()
        val saved = passages.repository.save(
            PassageViews.newEntity("조회", "본문", HashingEmbedder(64).embed("본문"))
        )

        assertEquals(saved.id, PassageLookup(passages.repository).get(saved.id!!).id)
    }
}

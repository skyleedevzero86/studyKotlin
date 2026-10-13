package com.sleekydz86.densevector2.unit.passage

import com.sleekydz86.densevector2.global.embedding.HashingEmbedder
import com.sleekydz86.densevector2.global.passage.PassageViews
import com.sleekydz86.densevector2.global.persistence.PassageLookup
import com.sleekydz86.densevector2.passage.get.PassageGetService
import com.sleekydz86.densevector2.support.InMemoryPassages
import kotlin.test.Test
import kotlin.test.assertEquals

class PassageGetServiceTest {
    @Test
    fun getReturnsStoredEmbedding() {
        val passages = InMemoryPassages()
        val vector = HashingEmbedder(64).embed("단건 본문")
        val saved = passages.repository.save(PassageViews.newEntity("단건", "단건 본문", vector))

        val detail = PassageGetService(PassageLookup(passages.repository)).get(saved.id!!)

        assertEquals(saved.id, detail.id)
        assertEquals(64, detail.dims)
        assertEquals(vector.toList(), detail.embedding)
    }
}

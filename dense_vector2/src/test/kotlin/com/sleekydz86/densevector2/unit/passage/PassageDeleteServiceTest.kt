package com.sleekydz86.densevector2.unit.passage

import com.sleekydz86.densevector2.global.embedding.HashingEmbedder
import com.sleekydz86.densevector2.global.error.VectorStudyException
import com.sleekydz86.densevector2.global.passage.PassageViews
import com.sleekydz86.densevector2.global.persistence.PassageLookup
import com.sleekydz86.densevector2.passage.delete.PassageDeleteService
import com.sleekydz86.densevector2.support.InMemoryPassages
import com.sleekydz86.densevector2.support.RecordingVectorWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PassageDeleteServiceTest {
    private val passages = InMemoryPassages()
    private val writer = RecordingVectorWriter()
    private val service = PassageDeleteService(
        passages.repository,
        PassageLookup(passages.repository),
        writer
    )

    @Test
    fun deleteRemovesIndexThenRow() {
        val saved = passages.repository.save(
            PassageViews.newEntity("삭제", "삭제할 본문", HashingEmbedder(64).embed("삭제할 본문"))
        )

        service.delete(saved.id!!)

        assertEquals(listOf(saved.id), writer.deleted)
        assertTrue(passages.rows.isEmpty())
    }

    @Test
    fun missingIdIs404() {
        val error = assertFailsWith<VectorStudyException> { service.delete(44) }

        assertEquals(404, error.status.value())
        assertTrue(error.message!!.contains("id=44"))
    }
}

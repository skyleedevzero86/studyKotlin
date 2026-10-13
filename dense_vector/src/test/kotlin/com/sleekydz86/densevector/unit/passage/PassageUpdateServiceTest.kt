package com.sleekydz86.densevector.unit.passage

import com.sleekydz86.densevector.global.embedding.HashingEmbedder
import com.sleekydz86.densevector.global.passage.PassageViews
import com.sleekydz86.densevector.global.persistence.PassageLookup
import com.sleekydz86.densevector.passage.update.PassageUpdateService
import com.sleekydz86.densevector.support.InMemoryPassages
import com.sleekydz86.densevector.support.RecordingVectorWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class PassageUpdateServiceTest {
    private val passages = InMemoryPassages()
    private val writer = RecordingVectorWriter()
    private val service = PassageUpdateService(
        passages.repository,
        PassageLookup(passages.repository),
        HashingEmbedder(64),
        writer
    )

    @Test
    fun updateRewritesStoredVector() {
        val created = passages.repository.save(
            PassageViews.newEntity("이전", "이전 본문", HashingEmbedder(64).embed("이전 본문"))
        )

        val detail = service.update(created.id!!, "  이후  ", "  바뀐 본문  ")

        assertEquals("이후", detail.title)
        assertEquals("바뀐 본문", detail.body)
        assertEquals("이후", writer.upserts.single().title)
        assertEquals(created.id, writer.upserts.single().id)
    }
}

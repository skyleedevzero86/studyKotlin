package com.sleekydz86.densevector.unit.seed

import com.sleekydz86.densevector.global.embedding.HashingEmbedder
import com.sleekydz86.densevector.global.error.VectorStudyException
import com.sleekydz86.densevector.global.passage.PassageViews
import com.sleekydz86.densevector.global.seed.PassageSeedService
import com.sleekydz86.densevector.support.InMemoryPassages
import com.sleekydz86.densevector.support.RecordingVectorWriter
import org.springframework.http.HttpStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PassageSeedServiceTest {
    private val passages = InMemoryPassages()
    private val writer = RecordingVectorWriter()
    private val service = PassageSeedService(passages.repository, HashingEmbedder(64), writer)

    @Test
    fun fillToDoesNothingWhenTargetIsAlreadyMet() {
        val embedder = HashingEmbedder(64)
        passages.repository.save(PassageViews.newEntity("있음", "본문", embedder.embed("본문")))

        assertEquals(0, service.fillTo(1))
        assertTrue(writer.upserts.isEmpty())
    }

    @Test
    fun fillToIndexesOnlyTheMissingCount() {
        val seeded = service.fillTo(2)

        assertEquals(2, seeded)
        assertEquals(listOf("가상 문단 0001", "가상 문단 0002"), writer.upserts.map { it.title })
        assertTrue(passages.rows.all { it.title.startsWith("가상 문단") })
    }

    @Test
    fun indexFailureRollsBackTheChunk() {
        writer.error = VectorStudyException(HttpStatus.BAD_GATEWAY, "Elasticsearch 일괄 색인 에 실패했습니다.")

        assertFailsWith<VectorStudyException> { service.fillTo(1) }

        assertTrue(passages.rows.isEmpty())
    }
}

package com.sleekydz86.densevector.unit.passage

import com.sleekydz86.densevector.global.embedding.HashingEmbedder
import com.sleekydz86.densevector.global.error.VectorStudyException
import com.sleekydz86.densevector.passage.create.PassageCreateService
import com.sleekydz86.densevector.support.InMemoryPassages
import com.sleekydz86.densevector.support.RecordingVectorWriter
import org.springframework.http.HttpStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PassageCreateServiceTest {
    private val passages = InMemoryPassages()
    private val writer = RecordingVectorWriter()
    private val service = PassageCreateService(passages.repository, HashingEmbedder(64), writer)

    @Test
    fun createTrimsTextAndIndexesSavedId() {
        val detail = service.create("  코사인  ", "  방향이 비슷하다  ")

        assertEquals("코사인", detail.title)
        assertEquals("방향이 비슷하다", detail.body)
        assertEquals(64, detail.embedding.size)
        assertEquals(detail.id, writer.upserts.single().id)
        assertEquals(1, passages.rows.size)
    }

    @Test
    fun indexFailureDeletesSavedRow() {
        writer.error = VectorStudyException(HttpStatus.BAD_GATEWAY, "Elasticsearch 색인 id=1 에 실패했습니다.")

        val error = assertFailsWith<VectorStudyException> {
            service.create("실패", "색인되지 않는 문단")
        }

        assertTrue(error.message!!.contains("색인 id=1"))
        assertTrue(passages.rows.isEmpty())
        assertTrue(writer.upserts.isEmpty())
    }
}

package com.sleekydz86.densevector.unit.passage

import com.sleekydz86.densevector.global.embedding.HashingEmbedder
import com.sleekydz86.densevector.global.error.VectorStudyException
import com.sleekydz86.densevector.global.passage.PassageViews
import com.sleekydz86.densevector.passage.list.PassageListService
import com.sleekydz86.densevector.support.InMemoryPassages
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PassageListServiceTest {
    private val passages = InMemoryPassages()
    private val service = PassageListService(passages.repository)

    @Test
    fun listReturnsRequestedPage() {
        val embedder = HashingEmbedder(64)
        passages.repository.save(PassageViews.newEntity("하나", "본문 하나", embedder.embed("본문 하나")))
        passages.repository.save(PassageViews.newEntity("둘", "본문 둘", embedder.embed("본문 둘")))

        val page = service.list(0, 1)

        assertEquals(0, page.page)
        assertEquals(1, page.size)
        assertEquals(2, page.totalElements)
        assertEquals(2, page.totalPages)
        assertEquals("하나", page.items.single().title)
    }

    @Test
    fun listRejectsSizeZero() {
        val error = assertFailsWith<VectorStudyException> { service.list(0, 0) }

        assertTrue(error.message!!.contains("입력값: 0"))
    }
}

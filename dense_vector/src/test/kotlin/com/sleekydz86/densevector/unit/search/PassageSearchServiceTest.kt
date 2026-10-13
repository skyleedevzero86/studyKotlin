package com.sleekydz86.densevector.unit.search

import com.sleekydz86.densevector.global.config.VectorProperties
import com.sleekydz86.densevector.global.elasticsearch.VectorHit
import com.sleekydz86.densevector.global.embedding.HashingEmbedder
import com.sleekydz86.densevector.global.passage.PassageViews
import com.sleekydz86.densevector.global.search.PassageSearchService
import com.sleekydz86.densevector.support.InMemoryPassages
import com.sleekydz86.densevector.support.RecordingVectorReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PassageSearchServiceTest {
    @Test
    fun searchSkipsHitsMissingFromMysqlAndScoresStoredVector() {
        val embedder = HashingEmbedder(64)
        val passages = InMemoryPassages()
        val body = "코사인 유사도는 방향이다"
        val saved = passages.repository.save(PassageViews.newEntity("저장 문단", body, embedder.embed(body)))
        val reader = RecordingVectorReader()
        reader.hits = listOf(VectorHit(saved.id!!, 0.8), VectorHit(99, 0.2))
        val service = PassageSearchService(passages.repository, embedder, reader, VectorProperties())

        val result = service.search("  $body  ", 5)

        assertEquals(64, result.dims)
        assertEquals("cosine", result.similarity)
        assertEquals(5, result.k)
        assertEquals(saved.id, result.hits.single().id)
        assertEquals(0.8, result.hits.single().esScore)
        assertTrue(result.hits.single().cosine > 0.99)
    }
}

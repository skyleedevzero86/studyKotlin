package com.sleekydz86.densevector.global.embedding

import com.sleekydz86.densevector.global.error.VectorStudyException
import org.springframework.http.HttpStatus
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HashingEmbedderTest {
    private val embedder = HashingEmbedder(64)

    @Test
    fun embedMatchesConfiguredDims() {
        assertEquals(64, embedder.embed("코사인 유사도").size)
    }

    @Test
    fun embedIsUnitLength() {
        val vector = embedder.embed("HNSW 인덱싱")
        val norm = sqrt(vector.sumOf { value -> (value * value).toDouble() })
        assertTrue(abs(norm - 1.0) < 1e-5)
    }

    @Test
    fun sameTextProducesSameVector() {
        val left = embedder.embed("dense_vector dims")
        val right = embedder.embed("dense_vector dims")
        assertTrue(left.contentEquals(right))
    }

    @Test
    fun relatedTextIsCloserThanUnrelatedText() {
        val base = embedder.embed("코사인 유사도는 두 벡터의 방향을 재는 방법이다")
        val related = embedder.embed("코사인 유사도는 두 벡터의 방향을 각도로 재는 방법이다")
        val unrelated = embedder.embed("오늘은 맑고 바람이 약하다")
        assertTrue(CosineSimilarity.score(base, related) > CosineSimilarity.score(base, unrelated))
    }

    @Test
    fun blankTextIsRejected() {
        val error = assertFailsWith<VectorStudyException> { embedder.embed("   ") }
        assertEquals(HttpStatus.BAD_REQUEST, error.status)
    }
}

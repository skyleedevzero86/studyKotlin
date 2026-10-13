package com.sleekydz86.densevector.global.embedding

import com.sleekydz86.densevector.global.error.VectorStudyException
import org.springframework.http.HttpStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CosineSimilarityTest {
    @Test
    fun identicalVectorsScoreOne() {
        val vector = floatArrayOf(1f, 0f, 0f)
        assertEquals(1.0, CosineSimilarity.score(vector, vector), 1e-6)
    }

    @Test
    fun orthogonalVectorsScoreZero() {
        val score = CosineSimilarity.score(floatArrayOf(1f, 0f), floatArrayOf(0f, 1f))
        assertEquals(0.0, score, 1e-6)
    }

    @Test
    fun oppositeVectorsScoreMinusOne() {
        val score = CosineSimilarity.score(floatArrayOf(1f, 0f), floatArrayOf(-1f, 0f))
        assertEquals(-1.0, score, 1e-6)
    }

    @Test
    fun differentLengthsAreRejected() {
        val error = assertFailsWith<VectorStudyException> {
            CosineSimilarity.score(floatArrayOf(1f), floatArrayOf(1f, 0f))
        }
        assertEquals(HttpStatus.BAD_REQUEST, error.status)
        assertTrue(error.message!!.contains("왼쪽: 1"))
    }
}

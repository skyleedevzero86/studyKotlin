package com.sleekydz86.densevector2.global.embedding

import com.sleekydz86.densevector2.global.error.VectorStudyException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class IndexOptionRulesTest {
    @Test
    fun acceptsHnswCosine() {
        IndexOptionRules.validate(64, "hnsw", "cosine", 16, 100)
    }

    @Test
    fun rejectsDimsAboveLimit() {
        val error = assertFailsWith<VectorStudyException> {
            IndexOptionRules.validate(4097, "hnsw", "cosine", 16, 100)
        }
        assertTrue(error.message!!.contains("4097"))
    }

    @Test
    fun rejectsDimsZero() {
        assertFailsWith<VectorStudyException> {
            IndexOptionRules.validate(0, "hnsw", "cosine", 16, 100)
        }
    }

    @Test
    fun rejectsOddDimsForInt4() {
        val error = assertFailsWith<VectorStudyException> {
            IndexOptionRules.validate(3, "int4_hnsw", "cosine", 16, 100)
        }
        assertTrue(error.message!!.contains("짝수"))
    }

    @Test
    fun acceptsBbqHnswAndBbqDisk() {
        IndexOptionRules.validate(64, "bbq_hnsw", "cosine", 16, 100)
        IndexOptionRules.validate(64, "bbq_disk", "cosine", 16, 100)
    }

    @Test
    fun rejectsUnknownSimilarity() {
        assertFailsWith<VectorStudyException> {
            IndexOptionRules.validate(64, "hnsw", "euclidean", 16, 100)
        }
    }
}

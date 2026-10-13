package com.sleekydz86.densevector.global.embedding

import com.sleekydz86.densevector.global.error.VectorStudyException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DenseVectorMappingTest {
    @Test
    fun hnswMappingContainsCosineAndGraphParameters() {
        val json = DenseVectorMapping.createIndexJson(64, "cosine", "hnsw", 16, 100)
        assertTrue(json.contains("\"type\": \"dense_vector\""))
        assertTrue(json.contains("\"dims\": 64"))
        assertTrue(json.contains("\"similarity\": \"cosine\""))
        assertTrue(json.contains("\"type\": \"hnsw\""))
        assertTrue(json.contains("\"m\": 16"))
        assertTrue(json.contains("\"ef_construction\": 100"))
    }

    @Test
    fun flatMappingOmitsGraphParameters() {
        val json = DenseVectorMapping.createIndexJson(32, "dot_product", "flat", 16, 100)
        assertTrue(json.contains("\"type\": \"flat\""))
        assertTrue(!json.contains("ef_construction"))
    }

    @Test
    fun invalidTypeDoesNotBuildJson() {
        assertFailsWith<VectorStudyException> {
            DenseVectorMapping.createIndexJson(8, "cosine", "bbq_hnsw", 16, 100)
        }
    }
}

package com.sleekydz86.densevector2.unit.elasticsearch

import com.sleekydz86.densevector2.global.elasticsearch.VectorIndexInfoService
import com.sleekydz86.densevector2.support.RecordingIndexAdmin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VectorIndexInfoServiceTest {
    @Test
    fun describeAddsEmbedderAndVersionNote() {
        val response = VectorIndexInfoService(RecordingIndexAdmin()).describe()

        assertEquals("hashing-bigram", response.embedder)
        assertEquals("hnsw", response.indexType)
        assertTrue(response.note.contains("9.5.0"))
        assertTrue(response.note.contains("vectordb_document"))
        assertTrue(response.note.contains("언어 모델이 아닙니다"))
    }
}

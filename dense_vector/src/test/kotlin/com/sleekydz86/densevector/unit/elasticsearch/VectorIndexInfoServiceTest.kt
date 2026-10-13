package com.sleekydz86.densevector.unit.elasticsearch

import com.sleekydz86.densevector.global.elasticsearch.VectorIndexInfoService
import com.sleekydz86.densevector.support.RecordingIndexAdmin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VectorIndexInfoServiceTest {
    @Test
    fun describeAddsEmbedderAndVersionNote() {
        val response = VectorIndexInfoService(RecordingIndexAdmin()).describe()

        assertEquals("hashing-bigram", response.embedder)
        assertEquals("hnsw", response.indexType)
        assertTrue(response.note.contains("8.15.3"))
        assertTrue(response.note.contains("언어 모델이 아닙니다"))
    }
}

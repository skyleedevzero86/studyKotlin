package com.sleekydz86.youtubemsa.search

import org.junit.jupiter.api.DisplayName
import kotlin.test.Test
import kotlin.test.assertTrue

@DisplayName("단위: 벡터 인덱스 매핑")
class DenseVectorMappingTest {
    @Test
    @DisplayName("9.5 문서 모드와 bbq_hnsw 코사인 설정을 넣는다")
    fun writesVectorDbMapping() {
        // given
        val dims = 64

        // when
        val json = DenseVectorMapping.createIndexJson(dims, "cosine", "bbq_hnsw", 16, 100)

        // then
        assertTrue(json.contains("vectordb_document"))
        assertTrue(json.contains("\"type\": \"bbq_hnsw\""))
        assertTrue(json.contains("\"similarity\": \"cosine\""))
        assertTrue(json.contains("\"dims\": 64"))
    }
}

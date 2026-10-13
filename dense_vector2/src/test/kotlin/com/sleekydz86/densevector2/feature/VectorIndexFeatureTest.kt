package com.sleekydz86.densevector2.feature

import com.sleekydz86.densevector2.global.elasticsearch.IndexInfoResponse
import com.sleekydz86.densevector2.global.elasticsearch.VectorIndexController
import com.sleekydz86.densevector2.global.elasticsearch.VectorIndexInfoService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(VectorIndexController::class)
class VectorIndexFeatureTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var service: VectorIndexInfoService

    @Test
    fun indexReturnsConfiguredMapping() {
        whenever(service.describe()).thenReturn(
            IndexInfoResponse("dense-vector-passages", 64, "cosine", "hnsw", 16, 100, "8.15.3", "hashing-bigram", "설명")
        )

        mockMvc.perform(get("/api/vector-index"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.indexType").value("hnsw"))
            .andExpect(jsonPath("$.embedder").value("hashing-bigram"))
    }
}

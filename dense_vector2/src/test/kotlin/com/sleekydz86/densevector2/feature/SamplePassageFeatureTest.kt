package com.sleekydz86.densevector2.feature

import com.sleekydz86.densevector2.global.seed.SampleLoadResponse
import com.sleekydz86.densevector2.global.seed.SamplePassageController
import com.sleekydz86.densevector2.global.seed.SamplePassageLoader
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SamplePassageController::class)
class SamplePassageFeatureTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var loader: SamplePassageLoader

    @Test
    fun loadReturnsInsertedCount() {
        whenever(loader.load()).thenReturn(6)

        mockMvc.perform(post("/api/passages/samples"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.loaded").value(6))
    }
}

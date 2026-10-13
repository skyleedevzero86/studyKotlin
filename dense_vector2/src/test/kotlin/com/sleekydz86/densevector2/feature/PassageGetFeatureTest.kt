package com.sleekydz86.densevector2.feature

import com.sleekydz86.densevector2.global.error.VectorStudyException
import com.sleekydz86.densevector2.global.passage.PassageDetail
import com.sleekydz86.densevector2.passage.get.PassageGetController
import com.sleekydz86.densevector2.passage.get.PassageGetService
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PassageGetController::class)
class PassageGetFeatureTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var service: PassageGetService

    @Test
    fun getReturnsEmbedding() {
        whenever(service.get(1L)).thenReturn(PassageDetail(1, "단건", "본문", 64, null, listOf(0.5f)))

        mockMvc.perform(get("/api/passages/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("단건"))
            .andExpect(jsonPath("$.embedding[0]").value(0.5))
    }

    @Test
    fun missingPassageReturns404Message() {
        whenever(service.get(9L)).thenThrow(
            VectorStudyException(HttpStatus.NOT_FOUND, "문단을 찾지 못했습니다. id=9")
        )

        mockMvc.perform(get("/api/passages/9"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message", containsString("id=9")))
    }
}

package com.sleekydz86.densevector.feature

import com.sleekydz86.densevector.global.passage.PassageSummary
import com.sleekydz86.densevector.passage.list.PassageListController
import com.sleekydz86.densevector.passage.list.PassageListService
import com.sleekydz86.densevector.passage.list.PassagePageResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PassageListController::class)
class PassageListFeatureTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var service: PassageListService

    @Test
    fun listPassesPageAndSize() {
        whenever(service.list(1, 20)).thenReturn(
            PassagePageResponse(1, 20, 21, 2, listOf(PassageSummary(2, "둘째", "본문", 64, null)))
        )

        mockMvc.perform(get("/api/passages").param("page", "1").param("size", "20"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.page").value(1))
            .andExpect(jsonPath("$.totalElements").value(21))
            .andExpect(jsonPath("$.items[0].title").value("둘째"))
    }
}

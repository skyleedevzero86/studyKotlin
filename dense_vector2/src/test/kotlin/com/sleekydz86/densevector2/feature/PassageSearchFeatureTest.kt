package com.sleekydz86.densevector2.feature

import com.sleekydz86.densevector2.global.search.PassageSearchController
import com.sleekydz86.densevector2.global.search.PassageSearchService
import com.sleekydz86.densevector2.global.search.SearchHitResponse
import com.sleekydz86.densevector2.global.search.SearchResponseBody
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PassageSearchController::class)
class PassageSearchFeatureTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var service: PassageSearchService

    @Test
    fun searchReturnsScoreFields() {
        whenever(service.search(eq("코사인"), any())).thenReturn(
            SearchResponseBody(64, "cosine", 5, listOf(SearchHitResponse(1, "코사인 유사도", "본문", 0.78, 0.57)))
        )

        mockMvc.perform(
            post("/api/passages/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"query":"코사인","k":5}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.hits[0].cosine").value(0.57))
            .andExpect(jsonPath("$.hits[0].esScore").value(0.78))
    }

    @Test
    fun blankQueryReturnsKoreanValidationMessage() {
        mockMvc.perform(
            post("/api/passages/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"query":" "}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message", containsString("검색어는 비어 있을 수 없습니다.")))
    }
}

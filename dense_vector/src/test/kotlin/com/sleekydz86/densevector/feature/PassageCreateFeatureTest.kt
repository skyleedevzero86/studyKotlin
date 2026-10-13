package com.sleekydz86.densevector.feature

import com.sleekydz86.densevector.global.passage.PassageDetail
import com.sleekydz86.densevector.passage.create.PassageCreateController
import com.sleekydz86.densevector.passage.create.PassageCreateService
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PassageCreateController::class)
class PassageCreateFeatureTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var service: PassageCreateService

    @Test
    fun createReturns201AndBody() {
        whenever(service.create(any(), any())).thenReturn(
            PassageDetail(1, "제목", "본문", 64, null, List(64) { 0f })
        )

        mockMvc.perform(
            post("/api/passages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"제목","body":"본문"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.dims").value(64))
    }

    @Test
    fun blankTitleReturnsKoreanValidationMessage() {
        mockMvc.perform(
            post("/api/passages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":" ","body":"본문"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message", containsString("제목은 비어 있을 수 없습니다.")))
    }
}

package com.sleekydz86.densevector2.feature

import com.sleekydz86.densevector2.global.passage.PassageDetail
import com.sleekydz86.densevector2.passage.update.PassageUpdateController
import com.sleekydz86.densevector2.passage.update.PassageUpdateService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PassageUpdateController::class)
class PassageUpdateFeatureTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var service: PassageUpdateService

    @Test
    fun updateReturnsChangedTitle() {
        whenever(service.update(eq(3L), any(), any())).thenReturn(
            PassageDetail(3, "수정", "본문", 64, null, List(64) { 0f })
        )

        mockMvc.perform(
            put("/api/passages/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"수정","body":"본문"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("수정"))
    }

    @Test
    fun blankBodyReturnsKoreanValidationMessage() {
        mockMvc.perform(
            put("/api/passages/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"수정","body":""}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message", containsString("본문은 비어 있을 수 없습니다.")))
    }
}

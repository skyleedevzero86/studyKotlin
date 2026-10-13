package com.sleekydz86.densevector2.feature

import com.sleekydz86.densevector2.passage.delete.PassageDeleteController
import com.sleekydz86.densevector2.passage.delete.PassageDeleteService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PassageDeleteController::class)
class PassageDeleteFeatureTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var service: PassageDeleteService

    @Test
    fun deleteReturns204() {
        mockMvc.perform(delete("/api/passages/8"))
            .andExpect(status().isNoContent)

        verify(service).delete(8L)
    }
}

package com.sleekydz86.youtubemsa.gateway

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("단위: 게이트웨이 OpenAPI")
class GatewayOpenApiTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("화면이 호출하는 다운로드 경로를 문서화한다")
    fun documentsPublicDownloads() {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.info.title").value("youtube_download_msa API"))
            .andExpect(jsonPath("$.paths['/api/downloads'].post.operationId").value("pasteDownload"))
            .andExpect(jsonPath("$.paths['/api/downloads'].get.operationId").value("listDownloads"))
            .andExpect(jsonPath("$.paths['/api/downloads/saved'].get.operationId").value("searchSaved"))
            .andExpect(jsonPath("$.paths['/api/downloads/search'].post.operationId").value("searchSimilar"))
            .andExpect(jsonPath("$.paths['/api/downloads/recommendations'].get.operationId").value("recommendDownloads"))
            .andExpect(jsonPath("$.paths['/api/downloads/{id}/recommendations'].get.operationId").value("recommendForDownload"))
            .andExpect(jsonPath("$.paths['/api/downloads/{id}/likes'].post.operationId").value("likeDownload"))
            .andExpect(jsonPath("$.paths['/api/downloads/{id}/dislikes'].post.operationId").value("dislikeDownload"))
            .andExpect(jsonPath("$.paths['/api/downloads/{id}/views'].post.operationId").value("viewDownload"))
            .andExpect(jsonPath("$.components.schemas.PasteBody").exists())
            .andExpect(jsonPath("$.components.schemas.ApiError").exists())
            .andExpect(jsonPath("$.paths['/internal/downloads/{id}']").doesNotExist())
    }
}

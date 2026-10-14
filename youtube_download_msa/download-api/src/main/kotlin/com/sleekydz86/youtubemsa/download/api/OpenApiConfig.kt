package com.sleekydz86.youtubemsa.download.api

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI = OpenAPI().info(
        Info()
            .title("youtube_download_msa download-api")
            .version("v1")
            .description("다운로드 원장 API입니다. 붙여 넣기는 /api/downloads, 워커와 검색이 쓰는 API는 /internal 입니다.")
    )
}

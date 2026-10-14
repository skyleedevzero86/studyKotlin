package com.sleekydz86.youtubemsa.search

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI = OpenAPI().info(
        Info()
            .title("youtube_download_msa search-api")
            .version("v1")
            .description("목록, 저장본 검색, 벡터 검색, 추천 5개, 반응 API입니다.")
    )
}

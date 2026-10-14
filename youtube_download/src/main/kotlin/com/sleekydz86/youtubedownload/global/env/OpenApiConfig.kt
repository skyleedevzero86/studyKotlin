package com.sleekydz86.youtubedownload.global.env

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI = OpenAPI().info(
        Info()
            .title("youtube_download API")
            .version("v1")
            .description("유튜브 공유 주소를 접수하고, 목록·저장본 검색·벡터 검색·추천·반응을 제공하는 API입니다.")
    )
}

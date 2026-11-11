package com.sleekydz86.cdcmigration.global.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openApi(): OpenAPI = OpenAPI().info(
        Info().title("CDC 이관 실습 API").version("0.0.1")
            .description("합성 데이터로 CSV 일회성 이관, 첨부파일 이관, Debezium CDC 변경분 반영을 실습합니다."),
    )
}

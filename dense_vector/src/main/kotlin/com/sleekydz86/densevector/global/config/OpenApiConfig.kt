package com.sleekydz86.densevector.global.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI = OpenAPI().info(
        Info()
            .title("dense_vector API")
            .version("v1")
            .description("MySQL에 원문을 두고 Elasticsearch dense_vector로 코사인 kNN 검색을 하는 학습 API입니다.")
    )
}

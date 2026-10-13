package com.sleekydz86.densevector2.global.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI = OpenAPI().info(
        Info()
            .title("dense_vector2 API")
            .version("v1")
            .description("Elasticsearch 9.5의 vectordb_document 인덱스에서 bbq_hnsw 또는 bbq_disk로 코사인 kNN을 보는 학습 API입니다.")
    )
}

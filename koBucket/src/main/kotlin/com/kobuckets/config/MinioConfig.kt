package com.kobuckets.config

import io.minio.MinioClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.boot.context.properties.EnableConfigurationProperties

@Configuration
@EnableConfigurationProperties(MinioProperties::class)
class MinioConfig {

    @Bean
    fun minioClient(properties: MinioProperties): MinioClient =
        MinioClient.builder()
            .endpoint(properties.url)
            .credentials(properties.accessKey, properties.secretKey)
            .build()
}

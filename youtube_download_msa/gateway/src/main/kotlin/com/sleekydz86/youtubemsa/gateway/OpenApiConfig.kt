package com.sleekydz86.youtubemsa.gateway

import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun gatewaySpec(): OpenApiCustomizer = OpenApiCustomizer { target ->
        val spec = gatewayOpenApi()
        target.info = spec.info
        target.tags = spec.tags
        target.components = spec.components
        target.paths = spec.paths
    }
}

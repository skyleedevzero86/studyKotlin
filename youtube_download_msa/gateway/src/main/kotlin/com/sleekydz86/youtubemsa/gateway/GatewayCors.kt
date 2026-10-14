package com.sleekydz86.youtubemsa.gateway

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class GatewayCors : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://127.0.0.1:3001", "http://localhost:3001")
            .allowedMethods("GET", "POST")
    }
}

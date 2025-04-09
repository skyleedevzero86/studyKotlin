package com.kstomp.global

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            //.allowedOrigins("http://localhost:3000", "http://localhost:8081")
            .allowedOriginPatterns("*")
            .allowedMethods("*")
            .allowCredentials(true);
    }
}

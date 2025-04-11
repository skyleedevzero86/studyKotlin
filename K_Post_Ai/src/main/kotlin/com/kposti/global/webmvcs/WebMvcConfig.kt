package com.kposti.global.webmvcs

import com.kposti.global.app.AppConfig
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/gen/**")
            .addResourceLocations("file:///" + AppConfig.getGenFileDirPath() + "/")
    }
}

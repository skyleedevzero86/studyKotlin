package com.kominioai.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class WebFluxConfig : WebFluxConfigurer {

    @Bean
    fun apiRoutes(): RouterFunction<ServerResponse> = router {
        GET("/health") { ServerResponse.ok().bodyValue("OK") }
    }

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {

        configurer.defaultCodecs().maxInMemorySize(1024 * 1024)
    }
}
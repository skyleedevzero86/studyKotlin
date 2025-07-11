package com.kominioai.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/api/surveys/published", "/api/responses").permitAll()
                    .pathMatchers("/api/surveys/**").authenticated()
                    .anyExchange().authenticated()
            }
            .httpBasic { }
            .build()
    }
}
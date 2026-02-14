package com.sleekydz86.komfa.infrastructure.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.Customizer
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class MfaSecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .authorizeHttpRequests { authorize ->
            authorize
                .requestMatchers("/api/join", "/api/health").permitAll()
                .requestMatchers("/api/auth/find-username", "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                .requestMatchers("/ott/generate", "/login/**", "/ott/sent", "/reset-password", "/error", "/favicon.ico").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                .requestMatchers("/user", "/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
        }
        .formLogin { form ->
            form.defaultSuccessUrl("/", true)
        }
        .oneTimeTokenLogin(Customizer.withDefaults())
        .build()
}

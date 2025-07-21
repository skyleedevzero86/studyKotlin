package com.kominioai.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/actuator/**").permitAll()
                    .pathMatchers("/ws/**").permitAll()
                    .pathMatchers("/topic/**").permitAll()
                    .pathMatchers("/api/auth/**").permitAll()
                    .pathMatchers("/api/public/**").permitAll()
                    .pathMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                    .pathMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyExchange().authenticated()
            }
            .httpBasic { basic ->
                basic.authenticationManager(authenticationManager(userDetailsService()))
            }
            .csrf { csrf -> csrf.disable() }
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtDecoder(jwtDecoder())
                }
            }
            .build()
    }

    @Bean
    fun userDetailsService(): MapReactiveUserDetailsService {
        val user: UserDetails = User
            .withUsername("user")
            .password(passwordEncoder().encode("password"))
            .roles("USER")
            .build()
        return MapReactiveUserDetailsService(user)
    }

    @Bean
    fun authenticationManager(userDetailsService: MapReactiveUserDetailsService): ReactiveAuthenticationManager {
        return UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService).apply {
            setPasswordEncoder(passwordEncoder())
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun jwtDecoder(): ReactiveJwtDecoder {
        val secret = "your-secret-key-should-be-at-least-256-bits-long-for-security-purposes-and-should-be-kept-secret"
        val secretKey = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build()
    }

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val corsConfig = CorsConfiguration().apply {
            allowedOriginPatterns = listOf(
                "http://localhost:3000",
                "http://localhost:5000",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5000",
                "https://yourdomain.com",
                "*"
            )
            allowCredentials = true
            allowedHeaders = listOf(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "X-Requested-With",
                "X-CSRF-Token",
                "Cache-Control"
            )
            allowedMethods = listOf(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS",
                "HEAD",
                "PATCH"
            )
            exposedHeaders = listOf(
                "Authorization",
                "X-Total-Count",
                "X-Page-Count"
            )
            maxAge = 3600L
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", corsConfig)
        }
    }

    @Bean
    fun corsWebFilter(): CorsWebFilter {
        return CorsWebFilter(corsConfigurationSource())
    }
}
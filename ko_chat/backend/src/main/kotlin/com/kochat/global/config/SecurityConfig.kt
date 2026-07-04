package com.kochat.global.config

import com.kochat.adapter.inbound.security.LoginFilter
import com.kochat.domain.user.port.out.UserPersistencePort
import com.kochat.global.exception.ErrorResponseWriter
import com.kochat.global.security.RestAccessDeniedHandler
import com.kochat.global.security.RestAuthenticationEntryPoint
import com.kochat.global.security.jwt.JwtAuthenticationFilter
import com.kochat.global.security.jwt.JwtTokenProvider
import com.kochat.global.security.login.LoginAccountValidator
import com.kochat.global.security.login.LoginAuthenticationFailureHandler
import com.kochat.global.security.login.LoginSuccessHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.SecurityContextHolderFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import tools.jackson.databind.ObjectMapper

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val authenticationConfiguration: AuthenticationConfiguration,
    @Lazy private val loginSuccessHandler: LoginSuccessHandler,
    private val loginAuthenticationFailureHandler: LoginAuthenticationFailureHandler,
    private val jwtTokenProvider: JwtTokenProvider,
    private val loginAccountValidator: LoginAccountValidator,
    private val userPersistencePort: UserPersistencePort,
    private val errorResponseWriter: ErrorResponseWriter,
    private val objectMapper: ObjectMapper,
    private val restAuthenticationEntryPoint: RestAuthenticationEntryPoint,
    private val restAccessDeniedHandler: RestAccessDeniedHandler,
) {

    @Bean
    fun authenticationManager(configuration: AuthenticationConfiguration): AuthenticationManager =
        configuration.authenticationManager

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            addAllowedOrigin("http://localhost:3000")
            addAllowedMethod("*")
            addAllowedHeader("*")
            allowCredentials = true
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }

    @Bean
    fun loginFilter(authenticationManager: AuthenticationManager): LoginFilter =
        LoginFilter(
            authenticationManager = authenticationManager,
            authenticationSuccessHandler = loginSuccessHandler,
            authenticationFailureHandler = loginAuthenticationFailureHandler,
            loginAccountValidator = loginAccountValidator,
            errorResponseWriter = errorResponseWriter,
            objectMapper = objectMapper,
        )

    @Bean
    fun securityFilterChain(http: HttpSecurity, loginFilter: LoginFilter): SecurityFilterChain {
        http
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }

        http
            .csrf { it.disable() }

        http
            .formLogin { it.disable() }

        http
            .exceptionHandling { exception ->
                exception
                    .authenticationEntryPoint(restAuthenticationEntryPoint)
                    .accessDeniedHandler(restAccessDeniedHandler)
            }

        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                    ).permitAll()
                    .requestMatchers("/api/v1/join").permitAll()
                    .requestMatchers("/api/v1/login").permitAll()
                    .requestMatchers("/api/v1/user/password/change").permitAll()
                    .requestMatchers("/api/v1/").permitAll()
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    .requestMatchers("/api/v1/ws/**").permitAll()
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/v1/**").authenticated()
                    .anyRequest().denyAll()
            }

        http
            .addFilterAfter(
                JwtAuthenticationFilter(jwtTokenProvider, userPersistencePort, errorResponseWriter),
                SecurityContextHolderFilter::class.java,
            )

        http
            .addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter::class.java)

        http
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

        return http.build()
    }
}

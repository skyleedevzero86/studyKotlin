package com.komroonga.global.config

import com.komroonga.global.security.jwt.JwtAuthenticationFilter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        // URL 패턴 정의
        val publicUrls = arrayOf("/","/index","/login", "/auth/**", "/css/**", "/js/**",
            "/images/**","/gen/images/**","/*.jpg", "/*.png", "/*.jpeg","/db/status",
            "/*.css","/*.js", "/*.gif","/*.svg","/*.woff","/*.woff2","/*.ttf",
            "/images/**", "/webjars/**")
        val publicPostUrls = arrayOf("/posts", "/posts/search", "/posts/{id}", "/posts/notices/all")
        val authenticatedUrls = arrayOf("/posts/notices/member", "/posts/create", "/posts/{id}/edit")
        val adminUrls = arrayOf("/members/**")

        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

            // 요청 URL 권한 설정
            .authorizeHttpRequests { authorize ->
                // 공개 URL 설정
                for (url in publicUrls) {
                    authorize.requestMatchers(AntPathRequestMatcher(url)).permitAll()
                }
                for (url in publicPostUrls) {
                    authorize.requestMatchers(AntPathRequestMatcher(url)).permitAll()
                }

                // 인증 필요 URL 설정
                for (url in authenticatedUrls) {
                    authorize.requestMatchers(AntPathRequestMatcher(url)).authenticated()
                }

                // 관리자 권한 필요 URL 설정
                for (url in adminUrls) {
                    authorize.requestMatchers(AntPathRequestMatcher(url)).hasRole("ADMIN")
                }

                // 나머지는 인증 필요
                authorize.anyRequest().authenticated()
            }

            // JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

            // 예외 처리
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증에 실패했습니다.")
                }
                it.accessDeniedHandler { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "권한이 없습니다.")
                }
            }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
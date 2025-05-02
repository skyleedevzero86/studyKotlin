package com.komroonga.global.config

import com.komroonga.global.security.jwt.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authorize ->
                authorize
                    // 정적 리소스 및 인증 관련 경로
                    .requestMatchers("/auth/**", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                    // 게시판 관련 공개 접근 경로
                    .requestMatchers("/posts", "/posts/search", "/posts/{id}", "/posts/notices/all").permitAll()
                    // 회원 전용 공지
                    .requestMatchers("/posts/notices/member").authenticated()
                    // 관리자 전용 경로
                    .requestMatchers("/members/search").hasRole("ADMIN")
                    // 게시글 작성 및 수정은 로그인 필요
                    .requestMatchers("/posts/create", "/posts/{id}/edit").authenticated()
                    // 그 외 모든 요청은 인증 필요
                    .anyRequest().authenticated()
            }
            .formLogin { login ->
                login
                    .loginPage("/auth/login")
                    .permitAll()
            }
            .logout { logout ->
                logout
                    .logoutUrl("/auth/logout")
                    .logoutSuccessUrl("/auth/login?logout")
                    .permitAll()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }
}

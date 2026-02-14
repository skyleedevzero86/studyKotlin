package com.sleekydz86.komfa.infrastructure.security

import jakarta.servlet.DispatcherType
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.Customizer
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcherEntry
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class MfaSecurityConfig {

    @Value("\${komfa.frontend.base-url:}")
    private val frontendBaseUrl: String = ""

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests { authorize ->
            authorize
                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/me").permitAll()
                .requestMatchers("/api/join", "/api/health").permitAll()
                .requestMatchers("/api/auth/find-username", "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                .requestMatchers("/ott/generate", "/login/**", "/ott/sent", "/reset-password", "/error", "/favicon.ico").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                .requestMatchers("/user", "/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
        }
        .csrf { csrf ->
            csrf.ignoringRequestMatchers("/api/**", "/login", "/login/**", "/logout", "/logout/**", "/ott", "/ott/**")
        }
        .exceptionHandling { exceptions ->
            exceptions.authenticationEntryPoint(
                DelegatingAuthenticationEntryPoint(
                    LoginUrlAuthenticationEntryPoint("/login"),
                    RequestMatcherEntry(PathPatternRequestMatcher.withDefaults().matcher("/api/**"), Http401AuthenticationEntryPoint()),
                ),
            )
        }
        .formLogin { form ->
            val successUrl = frontendBaseUrl.trim().ifEmpty { null }?.let { u -> u.trimEnd('/') + "/" } ?: "/"
            form.successHandler(SimpleUrlAuthenticationSuccessHandler().apply {
                setDefaultTargetUrl(successUrl)
                setAlwaysUseDefaultTargetUrl(true)
            })
        }
        .logout { logout ->
            frontendBaseUrl.trim().ifEmpty { null }?.let { url ->
                logout.logoutSuccessUrl(url.trimEnd('/') + "/")
            }
        }
        .oneTimeTokenLogin(Customizer.withDefaults())
        .build()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("http://localhost:5173", "http://127.0.0.1:5173")
            allowedMethods = listOf("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}

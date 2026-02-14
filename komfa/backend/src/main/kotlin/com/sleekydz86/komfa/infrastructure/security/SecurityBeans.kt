package com.sleekydz86.komfa.infrastructure.security

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class SecurityBeans {

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun getMeNoErrorFilter(): FilterRegistrationBean<GetMeNoErrorFilter> =
        FilterRegistrationBean<GetMeNoErrorFilter>(GetMeNoErrorFilter()).apply {
            order = Ordered.HIGHEST_PRECEDENCE
        }
}

package com.karchitecture.global.config.auth

import com.karchitecture.global.config.auth.support.HttpMethod.*
import com.karchitecture.global.annotation.ParseMemberIdFromTokenInterceptor
import com.karchitecture.global.config.auth.interceptor.LoginValidCheckerInterceptor
import com.karchitecture.global.config.auth.interceptor.PathMatcherInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class AuthConfig(
    private val parseMemberIdFromTokenInterceptor: ParseMemberIdFromTokenInterceptor,
    private val loginValidCheckerInterceptor: LoginValidCheckerInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(parseMemberIdFromTokenInterceptor())
        registry.addInterceptor(loginValidCheckerInterceptor())
    }

    private fun parseMemberIdFromTokenInterceptor(): HandlerInterceptor {
        return PathMatcherInterceptor(parseMemberIdFromTokenInterceptor)
            .excludePathPattern("/**", OPTIONS)
    }

    private fun loginValidCheckerInterceptor(): HandlerInterceptor {
        return PathMatcherInterceptor(loginValidCheckerInterceptor)
            .excludePathPattern("/**", OPTIONS)
            .excludePathPattern("/auth", POST, GET)
            .addPathPatterns("/auth/test", GET, POST, PATCH, DELETE)
    }
}
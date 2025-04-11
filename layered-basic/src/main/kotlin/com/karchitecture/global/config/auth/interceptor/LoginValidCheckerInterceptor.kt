package com.karchitecture.global.config.auth.interceptor

import com.karchitecture.auth.application.TokenProvider
import com.karchitecture.global.config.auth.support.AuthenticationContext
import com.karchitecture.global.config.auth.support.AuthenticationExtractor
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class LoginValidCheckerInterceptor(
    private val tokenProvider: TokenProvider,
    private val authenticationContext: AuthenticationContext
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val token = AuthenticationExtractor.extract(request)
            .orElseThrow { IllegalArgumentException("토큰의 서명이 잘못 되었습니다.") }

        val memberId = tokenProvider.extract(token)
        authenticationContext.setAuthentication(memberId)

        return true
    }
}
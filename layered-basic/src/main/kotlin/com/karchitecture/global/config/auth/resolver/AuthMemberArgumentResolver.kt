package com.karchitecture.global.config.auth.resolver

import com.karchitecture.global.annotation.AuthMember
import com.karchitecture.global.config.auth.support.AuthenticationContext
import org.springframework.core.MethodParameter
import org.springframework.lang.Nullable
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AuthMemberArgumentResolver(
    private val authenticationContext: AuthenticationContext
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(AuthMember::class.java) &&
                parameter.parameterType == Long::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        @Nullable mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        @Nullable binderFactory: WebDataBinderFactory?
    ): Any? {
        return authenticationContext.getPrincipal().takeIf { it != ANONYMOUS_AUTH_ID }
            ?: throw IllegalArgumentException("로그인 정보를 찾을 수 없습니다.")
    }

    companion object {
        private const val ANONYMOUS_AUTH_ID = -1L
    }
}
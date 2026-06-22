package com.kochat.global.security

import com.kochat.global.exception.ApiErrorResponse
import com.kochat.global.exception.ErrorCode
import com.kochat.global.exception.ErrorResponseWriter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class RestAuthenticationEntryPoint(
    private val errorResponseWriter: ErrorResponseWriter,
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        errorResponseWriter.write(
            response,
            HttpStatus.UNAUTHORIZED.value(),
            ApiErrorResponse(
                error = ErrorCode.AUTHENTICATION_FAILED.defaultMessage,
                code = ErrorCode.AUTHENTICATION_FAILED.name,
            ),
        )
    }
}

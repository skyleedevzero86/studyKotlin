package com.sleekydz86.oauth.global.security.login

import com.sleekydz86.oauth.global.exception.ApiErrorResponse
import com.sleekydz86.oauth.global.exception.ErrorCode
import com.sleekydz86.oauth.global.exception.ErrorResponseWriter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component

@Component
class LoginAuthenticationFailureHandler(
    private val errorResponseWriter: ErrorResponseWriter,
) : AuthenticationFailureHandler {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        val message = when (exception) {
            is BadCredentialsException -> ErrorCode.AUTHENTICATION_FAILED.defaultMessage
            is DisabledException -> "비활성화된 계정입니다."
            else -> "인증에 실패했습니다."
        }

        errorResponseWriter.write(
            response,
            HttpStatus.UNAUTHORIZED.value(),
            ApiErrorResponse(
                error = message,
                code = ErrorCode.AUTHENTICATION_FAILED.name,
            ),
        )
    }
}

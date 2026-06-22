package com.kochat.global.security

import com.kochat.global.exception.ApiErrorResponse
import com.kochat.global.exception.ErrorCode
import com.kochat.global.exception.ErrorResponseWriter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class RestAccessDeniedHandler(
    private val errorResponseWriter: ErrorResponseWriter,
) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        errorResponseWriter.write(
            response,
            HttpStatus.FORBIDDEN.value(),
            ApiErrorResponse(
                error = ErrorCode.ACCESS_DENIED.defaultMessage,
                code = ErrorCode.ACCESS_DENIED.name,
            ),
        )
    }
}

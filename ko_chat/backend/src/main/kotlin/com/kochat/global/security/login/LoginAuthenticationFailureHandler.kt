package com.kochat.global.security.login

import com.kochat.domain.user.model.RecordLoginFailureCommand
import com.kochat.global.application.user.UserLifecycleApplicationService
import com.kochat.global.exception.ApiErrorResponse
import com.kochat.global.exception.ErrorCode
import com.kochat.global.exception.ErrorResponseWriter
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
    private val userLifecycleApplicationService: UserLifecycleApplicationService,
) : AuthenticationFailureHandler {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        val username = request.getAttribute(LOGIN_USERNAME_ATTRIBUTE) as? String
        val loginFailCount = if (exception is BadCredentialsException && username != null) {
            userLifecycleApplicationService.recordLoginFailure(RecordLoginFailureCommand(username))?.loginFailCount
        } else {
            null
        }

        val message = when (exception) {
            is BadCredentialsException -> {
                if (loginFailCount != null && loginFailCount > 0) {
                    "${ErrorCode.AUTHENTICATION_FAILED.defaultMessage} (로그인 실패 ${loginFailCount}회)"
                } else {
                    ErrorCode.AUTHENTICATION_FAILED.defaultMessage
                }
            }
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

    companion object {
        const val LOGIN_USERNAME_ATTRIBUTE = "loginUsername"
    }
}

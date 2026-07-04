package com.kochat.global.security.login

import com.kochat.domain.user.model.RecordLoginFailureCommand
import com.kochat.domain.user.model.User
import com.kochat.domain.user.model.UserStatus
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
        val failedUser = if (exception is BadCredentialsException && username != null) {
            userLifecycleApplicationService.recordLoginFailure(RecordLoginFailureCommand(username))
        } else {
            null
        }

        val message = when (exception) {
            is BadCredentialsException -> {
                when {
                    failedUser?.status == UserStatus.LOGIN_LOCKED ->
                        "로그인 실패 횟수(${User.MAX_LOGIN_FAILS}회)를 초과하여 계정이 잠겼습니다. 관리자에게 문의하세요."
                    failedUser != null && failedUser.loginFailCount > 0 ->
                        "${ErrorCode.AUTHENTICATION_FAILED.defaultMessage} (로그인 실패 ${failedUser.loginFailCount}/${User.MAX_LOGIN_FAILS}회)"
                    else ->
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

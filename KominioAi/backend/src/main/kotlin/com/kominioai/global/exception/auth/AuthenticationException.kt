package com.kominioai.global.exception.auth

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorType


sealed class AuthenticationException(
    errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : BaseException(errorCode, message, cause) {
    class AuthenticationFailedException(
        reason: String? = null,
        cause: Throwable? = null
    ) : AuthenticationException(
        errorCode = ErrorCode.AUTHENTICATION_FAILED,
        message = "인증에 실패했습니다${reason?.let { ". 사유: $it" } ?: ""}",
        cause = cause
    )

    class TokenExpiredException(
        cause: Throwable? = null
    ) : AuthenticationException(
        errorCode = ErrorCode.TOKEN_EXPIRED,
        message = "인증 토큰이 만료되었습니다",
        cause = cause
    )

    class InvalidCredentialsException(
        cause: Throwable? = null
    ) : AuthenticationException(
        errorCode = ErrorCode.INVALID_CREDENTIALS,
        message = "잘못된 인증 정보입니다",
        cause = cause
    )

    class AccountLockedException(
        reason: String? = null,
        cause: Throwable? = null
    ) : AuthenticationException(
        errorCode = ErrorCode.AUTHENTICATION_FAILED,
        message = "계정이 잠겼습니다${reason?.let { ". 사유: $it" } ?: ""}",
        cause = cause
    )

    class AccountDisabledException(
        cause: Throwable? = null
    ) : AuthenticationException(
        errorCode = ErrorCode.AUTHENTICATION_FAILED,
        message = "계정이 비활성화되었습니다",
        cause = cause
    )
} 
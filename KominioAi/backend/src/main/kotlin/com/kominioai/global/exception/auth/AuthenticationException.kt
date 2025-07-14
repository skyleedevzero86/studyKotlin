package com.kominioai.global.exception.auth

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorType

/**
 * 인증 관련 예외들
 */
sealed class AuthenticationException(
    message: String,
    errorCode: ErrorCode,
    cause: Throwable? = null,
    requestId: String? = null
) : BaseException(message, errorCode, ErrorType.AUTHENTICATION, cause, requestId = requestId) {

    /**
     * 인증 실패
     */
    class AuthenticationFailedException(
        reason: String? = null,
        cause: Throwable? = null,
        requestId: String? = null
    ) : AuthenticationException(
        message = "인증에 실패했습니다${reason?.let { ". 사유: $it" } ?: ""}",
        errorCode = ErrorCode.AUTHENTICATION_FAILED,
        cause = cause,
        requestId = requestId
    )

    /**
     * 토큰 만료
     */
    class TokenExpiredException(
        cause: Throwable? = null,
        requestId: String? = null
    ) : AuthenticationException(
        message = "인증 토큰이 만료되었습니다",
        errorCode = ErrorCode.TOKEN_EXPIRED,
        cause = cause,
        requestId = requestId
    )

    /**
     * 잘못된 인증 정보
     */
    class InvalidCredentialsException(
        cause: Throwable? = null,
        requestId: String? = null
    ) : AuthenticationException(
        message = "잘못된 인증 정보입니다",
        errorCode = ErrorCode.INVALID_CREDENTIALS,
        cause = cause,
        requestId = requestId
    )
} 
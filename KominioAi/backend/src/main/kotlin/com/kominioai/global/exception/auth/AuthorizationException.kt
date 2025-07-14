package com.kominioai.global.exception.auth

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorType

/**
 * 인가 관련 예외들
 */
sealed class AuthorizationException(
    message: String,
    errorCode: ErrorCode,
    cause: Throwable? = null,
    requestId: String? = null
) : BaseException(message, errorCode, ErrorType.AUTHORIZATION, cause, requestId = requestId) {

    /**
     * 접근 권한 없음
     */
    class AccessDeniedException(
        resource: String? = null,
        requiredRole: String? = null,
        cause: Throwable? = null,
        requestId: String? = null
    ) : AuthorizationException(
        message = buildString {
            append("접근 권한이 없습니다")
            resource?.let { append(" (리소스: $it)") }
            requiredRole?.let { append(" (필요 권한: $it)") }
        },
        errorCode = ErrorCode.ACCESS_DENIED,
        cause = cause,
        requestId = requestId
    )

    /**
     * 리소스 소유자가 아님
     */
    class NotResourceOwnerException(
        resourceId: String,
        resourceType: String,
        cause: Throwable? = null,
        requestId: String? = null
    ) : AuthorizationException(
        message = "$resourceType (ID: $resourceId)의 소유자가 아닙니다",
        errorCode = ErrorCode.ACCESS_DENIED,
        cause = cause,
        requestId = requestId
    )
} 
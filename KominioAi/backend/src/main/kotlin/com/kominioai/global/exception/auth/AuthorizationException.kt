package com.kominioai.global.exception.auth

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorType

sealed class AuthorizationException(
    errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : BaseException(errorCode, message, cause) {

    class AccessDeniedException(
        resource: String? = null,
        requiredPermission: String? = null,
        cause: Throwable? = null
    ) : AuthorizationException(
        errorCode = ErrorCode.ACCESS_DENIED,
        message = buildString {
            append("접근 권한이 없습니다")
            resource?.let { append(" (리소스: $it)") }
            requiredPermission?.let { append(" (필요 권한: $it)") }
        },
        cause = cause
    )

    class InsufficientRoleException(
        requiredRole: String,
        currentRole: String? = null,
        cause: Throwable? = null
    ) : AuthorizationException(
        errorCode = ErrorCode.ACCESS_DENIED,
        message = buildString {
            append("필요한 역할이 없습니다 (필요: $requiredRole)")
            currentRole?.let { append(", 현재: $it") }
        },
        cause = cause
    )

    class ResourceOwnershipException(
        resourceType: String,
        resourceId: String,
        cause: Throwable? = null
    ) : AuthorizationException(
        errorCode = ErrorCode.ACCESS_DENIED,
        message = "리소스에 대한 소유권이 없습니다 ($resourceType: $resourceId)",
        cause = cause
    )

    class SessionExpiredException(
        cause: Throwable? = null
    ) : AuthorizationException(
        errorCode = ErrorCode.TOKEN_EXPIRED,
        message = "세션이 만료되었습니다",
        cause = cause
    )

    class AccountLockedException(
        reason: String? = null,
        cause: Throwable? = null
    ) : AuthorizationException(
        errorCode = ErrorCode.ACCESS_DENIED,
        message = "계정이 잠겼습니다${reason?.let { ". 사유: $it" } ?: ""}",
        cause = cause
    )
} 
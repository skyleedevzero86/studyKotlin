package com.kominioai.global.exception.infrastructure

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorType

sealed class InfrastructureException(
    errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : BaseException(errorCode, message, cause) {

    class DatabaseConnectionFailedException(
        cause: Throwable? = null
    ) : InfrastructureException(
        errorCode = ErrorCode.DATABASE_CONNECTION_FAILED,
        message = "데이터베이스 연결에 실패했습니다",
        cause = cause
    )

    class CacheOperationFailedException(
        operation: String,
        cause: Throwable? = null
    ) : InfrastructureException(
        errorCode = ErrorCode.CACHE_OPERATION_FAILED,
        message = "캐시 작업에 실패했습니다: $operation",
        cause = cause
    )

    class FileOperationFailedException(
        operation: String,
        fileName: String? = null,
        cause: Throwable? = null
    ) : InfrastructureException(
        errorCode = ErrorCode.FILE_OPERATION_FAILED,
        message = buildString {
            append("파일 작업에 실패했습니다: $operation")
            fileName?.let { append(" (파일: $it)") }
        },
        cause = cause
    )

    class NetworkConnectionFailedException(
        endpoint: String,
        cause: Throwable? = null
    ) : InfrastructureException(
        errorCode = ErrorCode.DATABASE_CONNECTION_FAILED,
        message = "네트워크 연결에 실패했습니다: $endpoint",
        cause = cause
    )

    class ResourceExhaustedException(
        resourceType: String,
        cause: Throwable? = null
    ) : InfrastructureException(
        errorCode = ErrorCode.DATABASE_CONNECTION_FAILED,
        cause = cause
    )
} 
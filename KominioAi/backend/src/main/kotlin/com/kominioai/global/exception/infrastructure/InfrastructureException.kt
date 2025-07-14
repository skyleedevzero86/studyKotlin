package com.kominioai.global.exception.infrastructure

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorType

/**
 * 인프라스트럭처 관련 예외들
 */
sealed class InfrastructureException(
    message: String,
    errorCode: ErrorCode,
    cause: Throwable? = null,
    requestId: String? = null
) : BaseException(message, errorCode, ErrorType.INFRASTRUCTURE, cause, requestId = requestId) {

    /**
     * 데이터베이스 연결 실패
     */
    class DatabaseConnectionFailedException(
        cause: Throwable? = null,
        requestId: String? = null
    ) : InfrastructureException(
        message = "데이터베이스 연결에 실패했습니다",
        errorCode = ErrorCode.DATABASE_CONNECTION_FAILED,
        cause = cause,
        requestId = requestId
    )

    /**
     * 캐시 작업 실패
     */
    class CacheOperationFailedException(
        operation: String,
        cause: Throwable? = null,
        requestId: String? = null
    ) : InfrastructureException(
        message = "캐시 작업에 실패했습니다: $operation",
        errorCode = ErrorCode.CACHE_OPERATION_FAILED,
        cause = cause,
        requestId = requestId
    )

    /**
     * 파일 작업 실패
     */
    class FileOperationFailedException(
        operation: String,
        fileName: String? = null,
        cause: Throwable? = null,
        requestId: String? = null
    ) : InfrastructureException(
        message = buildString {
            append("파일 작업에 실패했습니다: $operation")
            fileName?.let { append(" (파일: $it)") }
        },
        errorCode = ErrorCode.FILE_OPERATION_FAILED,
        cause = cause,
        requestId = requestId
    )
} 
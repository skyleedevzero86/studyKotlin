package com.kominioai.global.exception.infrastructure

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorType

/**
 * 외부 시스템 통합 관련 예외들
 */
sealed class IntegrationException(
    message: String,
    errorCode: ErrorCode,
    cause: Throwable? = null,
    requestId: String? = null
) : BaseException(message, errorCode, ErrorType.INTEGRATION, cause, requestId = requestId) {

    /**
     * 외부 API 타임아웃
     */
    class ExternalApiTimeoutException(
        apiName: String,
        timeout: String,
        cause: Throwable? = null,
        requestId: String? = null
    ) : IntegrationException(
        message = "외부 API 호출 시간이 초과되었습니다: $apiName (제한시간: $timeout)",
        errorCode = ErrorCode.EXTERNAL_API_TIMEOUT,
        cause = cause,
        requestId = requestId
    )

    /**
     * 외부 API 호출 실패
     */
    class ExternalApiCallFailedException(
        apiName: String,
        statusCode: Int? = null,
        cause: Throwable? = null,
        requestId: String? = null
    ) : IntegrationException(
        message = buildString {
            append("외부 API 호출에 실패했습니다: $apiName")
            statusCode?.let { append(" (상태 코드: $it)") }
        },
        errorCode = ErrorCode.EXTERNAL_API_TIMEOUT,
        cause = cause,
        requestId = requestId
    )
} 
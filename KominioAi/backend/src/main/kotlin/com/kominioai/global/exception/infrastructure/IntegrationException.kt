package com.kominioai.global.exception.infrastructure

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorType

sealed class IntegrationException(
    errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : BaseException(errorCode, message, cause) {

    class ExternalApiTimeoutException(
        apiName: String,
        timeout: String,
        cause: Throwable? = null
    ) : IntegrationException(
        errorCode = ErrorCode.EXTERNAL_API_TIMEOUT,
        message = "외부 API 호출 시간이 초과되었습니다: $apiName (제한시간: $timeout)",
        cause = cause
    )

    class ExternalApiCallFailedException(
        apiName: String,
        statusCode: Int? = null,
        cause: Throwable? = null
    ) : IntegrationException(
        errorCode = ErrorCode.EXTERNAL_API_TIMEOUT,
        message = buildString {
            append("외부 API 호출에 실패했습니다: $apiName")
            statusCode?.let { append(" (상태 코드: $it)") }
        },
        cause = cause
    )

    class ExternalApiResponseParseException(
        apiName: String,
        responseBody: String? = null,
        cause: Throwable? = null
    ) : IntegrationException(
        errorCode = ErrorCode.EXTERNAL_API_TIMEOUT,
        message = buildString {
            append("외부 API 응답 파싱에 실패했습니다: $apiName")
            responseBody?.let { append(" (응답: ${it.take(100)}...)") }
        },
        cause = cause
    )

    class ExternalApiAuthenticationException(
        apiName: String,
        cause: Throwable? = null
    ) : IntegrationException(
        errorCode = ErrorCode.EXTERNAL_API_TIMEOUT,
        message = "외부 API 인증에 실패했습니다: $apiName",
        cause = cause
    )

    class ExternalApiAuthorizationException(
        apiName: String,
        requiredPermission: String? = null,
        cause: Throwable? = null
    ) : IntegrationException(
        errorCode = ErrorCode.EXTERNAL_API_TIMEOUT,
        message = buildString {
            append("외부 API 접근 권한이 없습니다: $apiName")
            requiredPermission?.let { append(" (필요 권한: $it)") }
        },
        cause = cause
    )

    class ExternalApiServiceUnavailableException(
        apiName: String,
        retryAfter: String? = null,
        cause: Throwable? = null
    ) : IntegrationException(
        errorCode = ErrorCode.EXTERNAL_API_TIMEOUT,
        message = buildString {
            append("외부 API 서비스가 일시적으로 사용할 수 없습니다: $apiName")
            retryAfter?.let { append(" (재시도 시간: $it)") }
        },
        cause = cause
    )
} 
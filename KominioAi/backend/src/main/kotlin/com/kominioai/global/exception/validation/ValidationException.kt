package com.kominioai.global.exception.validation

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorType

/**
 * 검증 관련 예외들
 */
sealed class ValidationException(
    message: String,
    errorCode: ErrorCode,
    cause: Throwable? = null,
    requestId: String? = null
) : BaseException(message, errorCode, ErrorType.VALIDATION, cause, requestId = requestId) {

    /**
     * 일반 검증 실패
     */
    class ValidationFailedException(
        errors: List<FieldError>,
        cause: Throwable? = null,
        requestId: String? = null
    ) : ValidationException(
        message = "입력 데이터 검증에 실패했습니다",
        errorCode = ErrorCode.VALIDATION_FAILED,
        cause = cause,
        requestId = requestId
    ) {
        val fieldErrors: List<FieldError> = errors
    }

    /**
     * 필수 필드 누락
     */
    class RequiredFieldMissingException(
        fieldName: String,
        cause: Throwable? = null,
        requestId: String? = null
    ) : ValidationException(
        message = "필수 필드가 누락되었습니다: $fieldName",
        errorCode = ErrorCode.REQUIRED_FIELD_MISSING,
        cause = cause,
        requestId = requestId
    )

    /**
     * 잘못된 형식
     */
    class InvalidFormatException(
        fieldName: String,
        expectedFormat: String,
        actualValue: String? = null,
        cause: Throwable? = null,
        requestId: String? = null
    ) : ValidationException(
        message = buildString {
            append("잘못된 형식입니다: $fieldName")
            append(" (예상 형식: $expectedFormat)")
            actualValue?.let { append(", 실제 값: $it") }
        },
        errorCode = ErrorCode.INVALID_FORMAT,
        cause = cause,
        requestId = requestId
    )
}

/**
 * 필드별 검증 오류 정보
 */
data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null,
    val errorCode: String? = null
) 
package com.kominioai.global.exception.validation

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorContext

sealed class ValidationException(
    errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null,
    context: ErrorContext? = null
) : BaseException(errorCode, message, cause, context) {

    class ValidationFailedException(
        val fieldErrors: List<FieldError>,
        cause: Throwable? = null,
        context: ErrorContext? = null
    ) : ValidationException(
        errorCode = ErrorCode.VALIDATION_FAILED,
        message = "입력 데이터 검증에 실패했습니다",
        cause = cause,
        context = context
    )

    class RequiredFieldMissingException(
        fieldName: String,
        cause: Throwable? = null,
        context: ErrorContext? = null
    ) : ValidationException(
        errorCode = ErrorCode.REQUIRED_FIELD_MISSING,
        message = "필수 필드가 누락되었습니다: $fieldName",
        cause = cause,
        context = context
    )

    class InvalidFormatException(
        fieldName: String,
        expectedFormat: String,
        actualValue: String? = null,
        cause: Throwable? = null,
        context: ErrorContext? = null
    ) : ValidationException(
        errorCode = ErrorCode.INVALID_FORMAT,
        message = buildString {
            append("잘못된 형식입니다: $fieldName")
            append(" (예상 형식: $expectedFormat)")
            actualValue?.let { append(", 실제 값: $it") }
        },
        cause = cause,
        context = context
    )
}
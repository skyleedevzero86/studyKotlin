package com.kominioai.global.exception.base

import java.time.Instant


abstract class BaseException(
    val errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null,
    val context: ErrorContext? = null
) : Exception(message ?: errorCode.description, cause) {

    val timestamp: Instant = Instant.now()
    val errorType: ErrorType = determineErrorType()

    private fun determineErrorType(): ErrorType {
        return when {
            errorCode.code.startsWith("SURVEY_") || errorCode.code.startsWith("QUESTION_") -> ErrorType.DOMAIN
            errorCode.code.startsWith("VALIDATION_") -> ErrorType.VALIDATION
            errorCode.code.startsWith("AUTH_") -> ErrorType.AUTHENTICATION
            errorCode.code.startsWith("SYS_") -> ErrorType.SYSTEM
            else -> ErrorType.SYSTEM
        }
    }

    override fun toString(): String {
        return "BaseException(errorCode=$errorCode, message='$message', timestamp=$timestamp, errorType=$errorType)"
    }
}
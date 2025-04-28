package com.komroonga.global.error.handler

import com.komroonga.global.error.model.AppError
import com.komroonga.global.utils.LoggerExtensions.logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalErrorHandler {
    // 로거 초기화
    private val log = logger()

    data class ErrorResponse(
        val message: String,
        val errorCode: String,
        val details: Map<String, Any>? = null
    )

    @ExceptionHandler(AppError::class)
    fun handleAppError(error: AppError): ResponseEntity<ErrorResponse> {
        log.error("Application error occurred: ${error.message}", error.cause)

        val (status, errorCode, details) = when (error) {
            is AppError.NotFound -> Triple(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                mapOf("resourceId" to (error.resourceId ?: "unknown"))
            )
            is AppError.Validation -> Triple(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                mapOf("field" to (error.field ?: "unknown"))
            )
            is AppError.BusinessRule -> Triple(
                HttpStatus.CONFLICT,
                "BUSINESS_RULE_VIOLATION",
                mapOf("ruleId" to (error.ruleId ?: "unknown"))
            )
            is AppError.ExternalService -> Triple(
                HttpStatus.SERVICE_UNAVAILABLE,
                "EXTERNAL_SERVICE_ERROR",
                mapOf("serviceName" to error.serviceName)
            )
            is AppError.System -> Triple(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "SYSTEM_ERROR",
                null
            )
        }

        return ResponseEntity
            .status(status)
            .body(ErrorResponse(error.message, errorCode, details))
    }
}
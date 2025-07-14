package com.kominioai.global.exception.monitoring

import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorContext
import com.kominioai.global.exception.base.ErrorSeverity
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class ErrorMetrics(
    private val meterRegistry: MeterRegistry
) {

    private val errorCounter = Counter.builder("application.errors.total")
        .description("Total number of application errors")
        .tag("type", "error")
        .register(meterRegistry)

    private val requestErrorTimer = Timer.builder("application.request.error.duration")
        .description("Request error handling duration")
        .tag("type", "error_handling")
        .register(meterRegistry)

    fun recordError(
        errorCode: ErrorCode,
        context: ErrorContext,
        duration: Duration
    ) {

        errorCounter.increment()

        val errorByCodeCounter = Counter.builder("application.errors.by.code")
            .description("Errors by error code")
            .tag("error_code", errorCode.code)
            .tag("http_status", errorCode.httpStatus.value().toString())
            .tag("severity", errorCode.severity.name)
            .register(meterRegistry)
        errorByCodeCounter.increment()

        val errorBySeverityCounter = Counter.builder("application.errors.by.severity")
            .description("Errors by severity level")
            .tag("severity", errorCode.severity.name)
            .register(meterRegistry)
        errorBySeverityCounter.increment()

        val errorByTypeCounter = Counter.builder("application.errors.by.type")
            .description("Errors by error type")
            .tag("error_type", errorCode.name)
            .register(meterRegistry)
        errorByTypeCounter.increment()

        requestErrorTimer.record(duration)

        recordAdditionalMetrics(errorCode, context)
    }

    fun recordDomainError(
        errorCode: ErrorCode,
        context: ErrorContext,
        duration: Duration
    ) {
        recordError(errorCode, context, duration)

        val domainCounter = Counter.builder("application.domain.errors")
            .description("Domain-specific errors")
            .tag("domain", extractDomainFromErrorCode(errorCode.code))
            .register(meterRegistry)

        domainCounter.increment()
    }


    fun recordValidationError(
        fieldErrors: List<com.kominioai.global.exception.validation.FieldError>,
        context: ErrorContext,
        duration: Duration
    ) {
        recordError(ErrorCode.VALIDATION_FAILED, context, duration)

        fieldErrors.forEach { fieldError ->
            val fieldErrorCounter = Counter.builder("application.validation.field.errors")
                .description("Field validation errors")
                .tag("field", fieldError.field)
                .tag("error_code", fieldError.errorCode ?: "UNKNOWN")
                .register(meterRegistry)

            fieldErrorCounter.increment()
        }
    }

    fun recordInfrastructureError(
        errorCode: ErrorCode,
        context: ErrorContext,
        duration: Duration
    ) {
        recordError(errorCode, context, duration)

        val infraCounter = Counter.builder("application.infrastructure.errors")
            .description("Infrastructure errors")
            .tag("component", extractComponentFromErrorCode(errorCode.code))
            .register(meterRegistry)

        infraCounter.increment()
    }

    private fun recordAdditionalMetrics(errorCode: ErrorCode, context: ErrorContext) {

        context.userId?.let { userId ->
            val userErrorCounter = Counter.builder("application.user.errors")
                .description("User-specific errors")
                .tag("user_id", userId)
                .register(meterRegistry)

            userErrorCounter.increment()
        }

        val pathErrorCounter = Counter.builder("application.path.errors")
            .description("Path-specific errors")
            .tag("path", context.requestPath ?: "unknown")
            .tag("method", context.requestMethod ?: "unknown")
            .register(meterRegistry)

        pathErrorCounter.increment()
    }

    private fun extractDomainFromErrorCode(errorCode: String): String {
        return errorCode.split("_").firstOrNull() ?: "UNKNOWN"
    }

    private fun extractComponentFromErrorCode(errorCode: String): String {
        return when {
            errorCode.startsWith("SYS_") -> "SYSTEM"
            errorCode.startsWith("DB_") -> "DATABASE"
            errorCode.startsWith("CACHE_") -> "CACHE"
            errorCode.startsWith("API_") -> "EXTERNAL_API"
            errorCode.startsWith("FILE_") -> "FILE_SYSTEM"
            else -> "UNKNOWN"
        }
    }
}
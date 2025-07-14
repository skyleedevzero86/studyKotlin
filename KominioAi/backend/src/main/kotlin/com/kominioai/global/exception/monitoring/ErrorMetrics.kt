package com.kominioai.global.exception.monitoring

import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorSeverity
import com.kominioai.global.exception.context.ErrorContext
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * 에러 모니터링 메트릭 컴포넌트
 */
@Component
class ErrorMetrics(
    private val meterRegistry: MeterRegistry
) {
    
    private val errorCounter = Counter.builder("application.errors.total")
        .description("Total number of application errors")
        .tag("type", "error")
        .register(meterRegistry)
    
    private val errorByCodeCounter = Counter.builder("application.errors.by.code")
        .description("Errors by error code")
        .tag("type", "error_code")
        .register(meterRegistry)
    
    private val errorBySeverityCounter = Counter.builder("application.errors.by.severity")
        .description("Errors by severity level")
        .tag("type", "severity")
        .register(meterRegistry)
    
    private val errorByTypeCounter = Counter.builder("application.errors.by.type")
        .description("Errors by error type")
        .tag("type", "error_type")
        .register(meterRegistry)
    
    private val requestErrorTimer = Timer.builder("application.request.error.duration")
        .description("Request error handling duration")
        .tag("type", "error_handling")
        .register(meterRegistry)
    
    /**
     * 에러 메트릭 기록
     */
    fun recordError(
        errorCode: ErrorCode,
        context: ErrorContext,
        duration: Duration
    ) {
        // 전체 에러 카운터 증가
        errorCounter.increment()
        
        // 에러 코드별 카운터 증가
        errorByCodeCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "error_code", errorCode.code,
                "http_status", errorCode.httpStatus.value().toString(),
                "severity", errorCode.severity.name
            )
        )
        
        // 심각도별 카운터 증가
        errorBySeverityCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "severity", errorCode.severity.name
            )
        )
        
        // 에러 타입별 카운터 증가
        errorByTypeCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "error_type", errorCode.name
            )
        )
        
        // 요청 에러 처리 시간 기록
        requestErrorTimer.record(duration)
        
        // 추가 메트릭
        recordAdditionalMetrics(errorCode, context)
    }
    
    /**
     * 도메인 에러 메트릭 기록
     */
    fun recordDomainError(
        errorCode: ErrorCode,
        context: ErrorContext,
        duration: Duration
    ) {
        recordError(errorCode, context, duration)
        
        // 도메인별 추가 메트릭
        val domainCounter = Counter.builder("application.domain.errors")
            .description("Domain-specific errors")
            .tag("domain", extractDomainFromErrorCode(errorCode.code))
            .register(meterRegistry)
        
        domainCounter.increment()
    }
    
    /**
     * 검증 에러 메트릭 기록
     */
    fun recordValidationError(
        fieldErrors: List<com.kominioai.global.exception.validation.FieldError>,
        context: ErrorContext,
        duration: Duration
    ) {
        recordError(ErrorCode.VALIDATION_FAILED, context, duration)
        
        // 필드별 검증 오류 메트릭
        fieldErrors.forEach { fieldError ->
            val fieldErrorCounter = Counter.builder("application.validation.field.errors")
                .description("Field validation errors")
                .tag("field", fieldError.field)
                .tag("error_code", fieldError.errorCode ?: "UNKNOWN")
                .register(meterRegistry)
            
            fieldErrorCounter.increment()
        }
    }
    
    /**
     * 인프라스트럭처 에러 메트릭 기록
     */
    fun recordInfrastructureError(
        errorCode: ErrorCode,
        context: ErrorContext,
        duration: Duration
    ) {
        recordError(errorCode, context, duration)
        
        // 인프라스트럭처별 추가 메트릭
        val infraCounter = Counter.builder("application.infrastructure.errors")
            .description("Infrastructure errors")
            .tag("component", extractComponentFromErrorCode(errorCode.code))
            .register(meterRegistry)
        
        infraCounter.increment()
    }
    
    private fun recordAdditionalMetrics(errorCode: ErrorCode, context: ErrorContext) {
        // 사용자별 에러 메트릭
        context.userId?.let { userId ->
            val userErrorCounter = Counter.builder("application.user.errors")
                .description("User-specific errors")
                .tag("user_id", userId)
                .register(meterRegistry)
            
            userErrorCounter.increment()
        }
        
        // 경로별 에러 메트릭
        val pathErrorCounter = Counter.builder("application.path.errors")
            .description("Path-specific errors")
            .tag("path", context.requestPath)
            .tag("method", context.requestMethod)
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
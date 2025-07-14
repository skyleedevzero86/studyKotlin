package com.kominioai.global.exception.logging

import com.kominioai.global.exception.base.ErrorCode
import com.kominioai.global.exception.base.ErrorSeverity
import com.kominioai.global.exception.context.ErrorContext
import java.time.Instant

/**
 * 구조화된 로그 엔트리
 */
data class StructuredLogEntry(
    val timestamp: Instant,
    val level: String,
    val errorCode: String,
    val errorType: String,
    val severity: ErrorSeverity,
    val message: String,
    val sanitizedMessage: String,
    val userId: String?,
    val requestId: String?,
    val traceId: String?,
    val spanId: String?,
    val requestPath: String,
    val requestMethod: String,
    val clientIp: String?,
    val userAgent: String?,
    val duration: Long,
    val environment: String,
    val version: String,
    val stackTrace: String?,
    val additionalInfo: Map<String, Any>,
    val cause: String?
) {
    companion object {
        fun fromException(
            exception: Exception,
            context: ErrorContext,
            additionalInfo: Map<String, Any> = emptyMap()
        ): StructuredLogEntry {
            val errorCode = when (exception) {
                is com.kominioai.global.exception.base.BaseException -> exception.errorCode
                else -> ErrorCode.UNEXPECTED_ERROR
            }
            
            return StructuredLogEntry(
                timestamp = Instant.now(),
                level = determineLogLevel(errorCode.severity),
                errorCode = errorCode.code,
                errorType = errorCode.name,
                severity = errorCode.severity,
                message = exception.message ?: "Unknown error",
                sanitizedMessage = sanitizeMessage(exception.message),
                userId = context.userId,
                requestId = context.requestId,
                traceId = context.traceId,
                spanId = context.spanId,
                requestPath = context.requestPath,
                requestMethod = context.requestMethod,
                clientIp = context.clientIp,
                userAgent = context.userAgent,
                duration = context.duration.toMillis(),
                environment = context.environment,
                version = context.version,
                stackTrace = if (errorCode.severity == ErrorSeverity.CRITICAL) {
                    getStackTrace(exception)
                } else null,
                additionalInfo = additionalInfo,
                cause = exception.cause?.message
            )
        }
        
        private fun determineLogLevel(severity: ErrorSeverity): String {
            return when (severity) {
                ErrorSeverity.INFO -> "INFO"
                ErrorSeverity.WARN -> "WARN"
                ErrorSeverity.ERROR -> "ERROR"
                ErrorSeverity.CRITICAL -> "ERROR"
            }
        }
        
        private fun sanitizeMessage(message: String?): String {
            if (message.isNullOrBlank()) return "Unknown error"
            
            // 민감 정보 마스킹
            return message
                .replace(Regex("password\\s*=\\s*[^\\s,}]+", RegexOption.IGNORE_CASE), "password=***")
                .replace(Regex("token\\s*=\\s*[^\\s,}]+", RegexOption.IGNORE_CASE), "token=***")
                .replace(Regex("secret\\s*=\\s*[^\\s,}]+", RegexOption.IGNORE_CASE), "secret=***")
        }
        
        private fun getStackTrace(exception: Exception): String {
            return exception.stackTraceToString()
        }
    }
} 
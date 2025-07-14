package com.kominioai.global.exception.logging

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.context.ErrorContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 구조화된 에러 로깅 컴포넌트
 */
@Component
class ErrorLogger {
    
    private val logger = LoggerFactory.getLogger(ErrorLogger::class.java)
    private val errorCounters = ConcurrentHashMap<String, Long>()
    
    /**
     * 에러 로깅
     */
    fun logError(
        exception: Exception,
        context: ErrorContext,
        additionalInfo: Map<String, Any> = emptyMap()
    ) {
        val logEntry = StructuredLogEntry.fromException(exception, context, additionalInfo)
        
        // 에러 카운터 증가
        incrementErrorCounter(logEntry.errorCode)
        
        // 로그 레벨에 따른 로깅
        when (logEntry.level) {
            "INFO" -> logger.info(logEntry.toString())
            "WARN" -> logger.warn(logEntry.toString())
            "ERROR" -> logger.error(logEntry.toString())
            else -> logger.error(logEntry.toString())
        }
        
        // 심각한 오류의 경우 추가 알림
        if (logEntry.severity == com.kominioai.global.exception.base.ErrorSeverity.CRITICAL) {
            logCriticalError(logEntry)
        }
    }
    
    /**
     * 도메인 예외 로깅
     */
    fun logDomainError(
        exception: BaseException,
        context: ErrorContext,
        additionalInfo: Map<String, Any> = emptyMap()
    ) {
        logError(exception, context, additionalInfo)
    }
    
    /**
     * 검증 오류 로깅
     */
    fun logValidationError(
        exception: com.kominioai.global.exception.validation.ValidationException,
        context: ErrorContext,
        fieldErrors: List<com.kominioai.global.exception.validation.FieldError>
    ) {
        val additionalInfo = mapOf(
            "fieldErrors" to fieldErrors.map { 
                mapOf(
                    "field" to it.field,
                    "message" to it.message,
                    "rejectedValue" to it.rejectedValue
                )
            }
        )
        logError(exception, context, additionalInfo)
    }
    
    /**
     * 인프라스트럭처 오류 로깅
     */
    fun logInfrastructureError(
        exception: com.kominioai.global.exception.infrastructure.InfrastructureException,
        context: ErrorContext,
        additionalInfo: Map<String, Any> = emptyMap()
    ) {
        logError(exception, context, additionalInfo)
    }
    
    private fun incrementErrorCounter(errorCode: String) {
        errorCounters.compute(errorCode) { _, count -> (count ?: 0) + 1 }
    }
    
    private fun logCriticalError(logEntry: StructuredLogEntry) {
        // 심각한 오류에 대한 추가 처리 (알림 발송 등)
        logger.error("CRITICAL ERROR DETECTED: ${logEntry.errorCode} - ${logEntry.message}")
        
        // TODO: 알림 시스템 연동 (Slack, Email 등)
        // notificationService.sendCriticalErrorAlert(logEntry)
    }
    
    /**
     * 에러 통계 조회
     */
    fun getErrorStatistics(): Map<String, Long> {
        return errorCounters.toMap()
    }
    
    override fun toString(): String {
        return "ErrorLogger(errorCounters=$errorCounters)"
    }
} 
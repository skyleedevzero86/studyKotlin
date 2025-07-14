package com.kominioai.global.exception.logging

import com.kominioai.global.exception.base.BaseException
import com.kominioai.global.exception.base.ErrorContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ErrorLogger {
    
    private val logger = LoggerFactory.getLogger(ErrorLogger::class.java)
    private val errorCounters = ConcurrentHashMap<String, Long>()

    fun logError(
        exception: Exception,
        context: ErrorContext,
        additionalInfo: Map<String, Any> = emptyMap()
    ) {
        val logEntry = StructuredLogEntry.fromException(exception, context, additionalInfo)

        incrementErrorCounter(logEntry.errorCode)

        when (logEntry.level) {
            "INFO" -> logger.info(logEntry.toString())
            "WARN" -> logger.warn(logEntry.toString())
            "ERROR" -> logger.error(logEntry.toString())
            else -> logger.error(logEntry.toString())
        }

        if (logEntry.severity == com.kominioai.global.exception.base.ErrorSeverity.CRITICAL) {
            logCriticalError(logEntry)
        }
    }

    fun logDomainError(
        exception: BaseException,
        context: ErrorContext,
        additionalInfo: Map<String, Any> = emptyMap()
    ) {
        logError(exception, context, additionalInfo)
    }

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

        logger.error("CRITICAL ERROR DETECTED: ${logEntry.errorCode} - ${logEntry.message}")
    }

    fun getErrorStatistics(): Map<String, Long> {
        return errorCounters.toMap()
    }
    
    override fun toString(): String {
        return "ErrorLogger(errorCounters=$errorCounters)"
    }
} 
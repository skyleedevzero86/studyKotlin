package com.kominioai.global.util

import org.slf4j.Logger
import org.slf4j.MDC
import java.util.*

/**
 * 구조화된 로깅을 위한 유틸리티 클래스
 */
object StructuredLogging {
    
    private const val REQUEST_ID_KEY = "requestId"
    private const val SURVEY_ID_KEY = "surveyId"
    private const val USER_ID_KEY = "userId"
    private const val QUESTION_ID_KEY = "questionId"
    private const val RESPONSE_ID_KEY = "responseId"
    private const val OPERATION_KEY = "operation"
    private const val DURATION_KEY = "duration"
    private const val STATUS_KEY = "status"
    private const val ERROR_CODE_KEY = "errorCode"
    private const val ERROR_MESSAGE_KEY = "errorMessage"
    
    /**
     * 요청 ID를 MDC에 설정
     */
    fun setRequestId(requestId: String) {
        MDC.put(REQUEST_ID_KEY, requestId)
    }
    
    /**
     * 설문조사 ID를 MDC에 설정
     */
    fun setSurveyId(surveyId: String) {
        MDC.put(SURVEY_ID_KEY, surveyId)
    }
    
    /**
     * 사용자 ID를 MDC에 설정
     */
    fun setUserId(userId: String) {
        MDC.put(USER_ID_KEY, userId)
    }
    
    /**
     * 질문 ID를 MDC에 설정
     */
    fun setQuestionId(questionId: String) {
        MDC.put(QUESTION_ID_KEY, questionId)
    }
    
    /**
     * 응답 ID를 MDC에 설정
     */
    fun setResponseId(responseId: String) {
        MDC.put(RESPONSE_ID_KEY, responseId)
    }
    
    /**
     * 작업 유형을 MDC에 설정
     */
    fun setOperation(operation: String) {
        MDC.put(OPERATION_KEY, operation)
    }
    
    /**
     * MDC 컨텍스트를 정리
     */
    fun clearContext() {
        MDC.clear()
    }
    
    /**
     * 구조화된 정보 로깅
     */
    fun logInfo(
        logger: Logger,
        message: String,
        vararg fields: Pair<String, Any?>
    ) {
        setFields(*fields)
        logger.info(message)
        clearFields(*fields.map { it.first }.toTypedArray())
    }
    
    /**
     * 구조화된 경고 로깅
     */
    fun logWarn(
        logger: Logger,
        message: String,
        vararg fields: Pair<String, Any?>
    ) {
        setFields(*fields)
        logger.warn(message)
        clearFields(*fields.map { it.first }.toTypedArray())
    }
    
    /**
     * 구조화된 오류 로깅
     */
    fun logError(
        logger: Logger,
        message: String,
        throwable: Throwable? = null,
        vararg fields: Pair<String, Any?>
    ) {
        setFields(*fields)
        if (throwable != null) {
            logger.error(message, throwable)
        } else {
            logger.error(message)
        }
        clearFields(*fields.map { it.first }.toTypedArray())
    }
    
    /**
     * 구조화된 디버그 로깅
     */
    fun logDebug(
        logger: Logger,
        message: String,
        vararg fields: Pair<String, Any?>
    ) {
        setFields(*fields)
        logger.debug(message)
        clearFields(*fields.map { it.first }.toTypedArray())
    }
    
    /**
     * 성능 측정 로깅
     */
    fun logPerformance(
        logger: Logger,
        operation: String,
        duration: Long,
        vararg fields: Pair<String, Any?>
    ) {
        val allFields = fields.toList() + (OPERATION_KEY to operation) + (DURATION_KEY to duration)
        logInfo(logger, "Performance measurement completed", *allFields.toTypedArray())
    }
    
    /**
     * API 요청 시작 로깅
     */
    fun logApiRequestStart(
        logger: Logger,
        method: String,
        path: String,
        requestId: String,
        userId: String? = null,
        vararg fields: Pair<String, Any?>
    ) {
        val allFields = fields.toList() + 
            ("method" to method) + 
            ("path" to path) + 
            (REQUEST_ID_KEY to requestId)
        
        if (userId != null) {
            setUserId(userId)
        }
        
        logInfo(logger, "API request started", *allFields.toTypedArray())
    }
    
    /**
     * API 요청 완료 로깅
     */
    fun logApiRequestComplete(
        logger: Logger,
        method: String,
        path: String,
        requestId: String,
        statusCode: Int,
        duration: Long,
        userId: String? = null,
        vararg fields: Pair<String, Any?>
    ) {
        val allFields = fields.toList() + 
            ("method" to method) + 
            ("path" to path) + 
            (REQUEST_ID_KEY to requestId) + 
            (STATUS_KEY to statusCode) + 
            (DURATION_KEY to duration)
        
        if (userId != null) {
            setUserId(userId)
        }
        
        logInfo(logger, "API request completed", *allFields.toTypedArray())
    }
    
    /**
     * 설문조사 관련 로깅
     */
    fun logSurveyOperation(
        logger: Logger,
        operation: String,
        surveyId: String,
        message: String,
        vararg fields: Pair<String, Any?>
    ) {
        val allFields = fields.toList() + 
            (OPERATION_KEY to operation) + 
            (SURVEY_ID_KEY to surveyId)
        
        logInfo(logger, message, *allFields.toTypedArray())
    }
    
    /**
     * 설문조사 오류 로깅
     */
    fun logSurveyError(
        logger: Logger,
        operation: String,
        surveyId: String,
        message: String,
        throwable: Throwable? = null,
        vararg fields: Pair<String, Any?>
    ) {
        val allFields = fields.toList() + 
            (OPERATION_KEY to operation) + 
            (SURVEY_ID_KEY to surveyId)
        
        logError(logger, message, throwable, *allFields.toTypedArray())
    }
    
    /**
     * 캐시 관련 로깅
     */
    fun logCacheOperation(
        logger: Logger,
        operation: String,
        cacheType: String,
        key: String,
        message: String,
        vararg fields: Pair<String, Any?>
    ) {
        val allFields = fields.toList() + 
            (OPERATION_KEY to operation) + 
            ("cacheType" to cacheType) + 
            ("cacheKey" to key)
        
        logInfo(logger, message, *allFields.toTypedArray())
    }
    
    /**
     * 필드들을 MDC에 설정
     */
    private fun setFields(vararg fields: Pair<String, Any?>) {
        fields.forEach { (key, value) ->
            if (value != null) {
                MDC.put(key, value.toString())
            }
        }
    }
    
    /**
     * 특정 필드들을 MDC에서 제거
     */
    private fun clearFields(vararg keys: String) {
        keys.forEach { key ->
            MDC.remove(key)
        }
    }
    
    /**
     * 고유한 요청 ID 생성
     */
    fun generateRequestId(): String {
        return UUID.randomUUID().toString()
    }
} 
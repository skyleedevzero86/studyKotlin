package com.kominioai.global.util

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.MDC
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StructuredLoggingTest {
    
    private lateinit var logger: Logger
    private lateinit var listAppender: ListAppender<ILoggingEvent>
    
    @BeforeEach
    fun setUp() {
        logger = LoggerFactory.getLogger("test") as Logger
        listAppender = ListAppender()
        listAppender.start()
        logger.addAppender(listAppender)
        logger.level = ch.qos.logback.classic.Level.INFO
        
        // MDC 초기화
        MDC.clear()
    }
    
    @Test
    fun `logInfo should log with structured fields`() {
        // Given
        val message = "Test info message"
        val fields = arrayOf("key1" to "value1", "key2" to "value2")
        
        // When
        StructuredLogging.logInfo(logger, message, *fields)
        
        // Then
        val events = listAppender.list
        assertEquals(1, events.size)
        
        val event = events[0]
        assertEquals(message, event.message)
        assertEquals(ch.qos.logback.classic.Level.INFO, event.level)
        
        // MDC에 필드가 설정되었는지 확인
        val mdc = event.mdcPropertyMap
        assertEquals("value1", mdc["key1"])
        assertEquals("value2", mdc["key2"])
    }
    
    @Test
    fun `logWarn should log with structured fields`() {
        // Given
        val message = "Test warning message"
        val fields = arrayOf("warningType" to "VALIDATION_ERROR")
        
        // When
        StructuredLogging.logWarn(logger, message, *fields)
        
        // Then
        val events = listAppender.list
        assertEquals(1, events.size)
        
        val event = events[0]
        assertEquals(message, event.message)
        assertEquals(ch.qos.logback.classic.Level.WARN, event.level)
        
        val mdc = event.mdcPropertyMap
        assertEquals("VALIDATION_ERROR", mdc["warningType"])
    }
    
    @Test
    fun `logError should log with throwable and structured fields`() {
        // Given
        val message = "Test error message"
        val throwable = RuntimeException("Test exception")
        val fields = arrayOf("errorCode" to "DB_CONNECTION_ERROR")
        
        // When
        StructuredLogging.logError(logger, message, throwable, *fields)
        
        // Then
        val events = listAppender.list
        assertEquals(1, events.size)
        
        val event = events[0]
        assertEquals(message, event.message)
        assertEquals(ch.qos.logback.classic.Level.ERROR, event.level)
        assertEquals(throwable, event.throwableProxy.throwable)
        
        val mdc = event.mdcPropertyMap
        assertEquals("DB_CONNECTION_ERROR", mdc["errorCode"])
    }
    
    @Test
    fun `logDebug should log with structured fields`() {
        // Given
        val message = "Test debug message"
        val fields = arrayOf("debugInfo" to "detailed info")
        
        // When
        StructuredLogging.logDebug(logger, message, *fields)
        
        // Then
        val events = listAppender.list
        assertEquals(1, events.size)
        
        val event = events[0]
        assertEquals(message, event.message)
        assertEquals(ch.qos.logback.classic.Level.DEBUG, event.level)
        
        val mdc = event.mdcPropertyMap
        assertEquals("detailed info", mdc["debugInfo"])
    }
    
    @Test
    fun `logPerformance should log performance metrics`() {
        // Given
        val operation = "DATABASE_QUERY"
        val duration = 150L
        val fields = arrayOf("queryType" to "SELECT", "tableName" to "surveys")
        
        // When
        StructuredLogging.logPerformance(logger, operation, duration, *fields)
        
        // Then
        val events = listAppender.list
        assertEquals(1, events.size)
        
        val event = events[0]
        assertEquals("Performance measurement completed", event.message)
        assertEquals(ch.qos.logback.classic.Level.INFO, event.level)
        
        val mdc = event.mdcPropertyMap
        assertEquals(operation, mdc["operation"])
        assertEquals(duration.toString(), mdc["duration"])
        assertEquals("SELECT", mdc["queryType"])
        assertEquals("surveys", mdc["tableName"])
    }
    
    @Test
    fun `logSurveyOperation should log survey specific information`() {
        // Given
        val operation = "CREATE_SURVEY"
        val surveyId = "survey-123"
        val message = "Survey created successfully"
        val fields = arrayOf("duration" to 245, "questionCount" to 5)
        
        // When
        StructuredLogging.logSurveyOperation(logger, operation, surveyId, message, *fields)
        
        // Then
        val events = listAppender.list
        assertEquals(1, events.size)
        
        val event = events[0]
        assertEquals(message, event.message)
        assertEquals(ch.qos.logback.classic.Level.INFO, event.level)
        
        val mdc = event.mdcPropertyMap
        assertEquals(operation, mdc["operation"])
        assertEquals(surveyId, mdc["surveyId"])
        assertEquals("245", mdc["duration"])
        assertEquals("5", mdc["questionCount"])
    }
    
    @Test
    fun `logSurveyError should log survey error with throwable`() {
        // Given
        val operation = "CREATE_SURVEY"
        val surveyId = "survey-123"
        val message = "Survey creation failed"
        val throwable = RuntimeException("Database connection failed")
        val fields = arrayOf("duration" to 123, "errorType" to "DB_ERROR")
        
        // When
        StructuredLogging.logSurveyError(logger, operation, surveyId, message, throwable, *fields)
        
        // Then
        val events = listAppender.list
        assertEquals(1, events.size)
        
        val event = events[0]
        assertEquals(message, event.message)
        assertEquals(ch.qos.logback.classic.Level.ERROR, event.level)
        assertEquals(throwable, event.throwableProxy.throwable)
        
        val mdc = event.mdcPropertyMap
        assertEquals(operation, mdc["operation"])
        assertEquals(surveyId, mdc["surveyId"])
        assertEquals("123", mdc["duration"])
        assertEquals("DB_ERROR", mdc["errorType"])
    }
    
    @Test
    fun `logCacheOperation should log cache specific information`() {
        // Given
        val operation = "CACHE_GET"
        val cacheType = "REDIS"
        val key = "survey:123"
        val message = "Cache hit"
        val fields = arrayOf("hit" to true, "ttl" to 300)
        
        // When
        StructuredLogging.logCacheOperation(logger, operation, cacheType, key, message, *fields)
        
        // Then
        val events = listAppender.list
        assertEquals(1, events.size)
        
        val event = events[0]
        assertEquals(message, event.message)
        assertEquals(ch.qos.logback.classic.Level.INFO, event.level)
        
        val mdc = event.mdcPropertyMap
        assertEquals(operation, mdc["operation"])
        assertEquals(cacheType, mdc["cacheType"])
        assertEquals(key, mdc["cacheKey"])
        assertEquals("true", mdc["hit"])
        assertEquals("300", mdc["ttl"])
    }
    
    @Test
    fun `generateRequestId should generate unique request IDs`() {
        // When
        val requestId1 = StructuredLogging.generateRequestId()
        val requestId2 = StructuredLogging.generateRequestId()
        
        // Then
        assertNotNull(requestId1)
        assertNotNull(requestId2)
        assertTrue(requestId1.isNotBlank())
        assertTrue(requestId2.isNotBlank())
        assertTrue(requestId1 != requestId2)
        
        // UUID 형식 검증
        val uuidRegex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
        assertTrue(uuidRegex.matches(requestId1))
        assertTrue(uuidRegex.matches(requestId2))
    }
    
    @Test
    fun `MDC context should be cleared after logging`() {
        // Given
        val message = "Test message"
        val fields = arrayOf("key1" to "value1")
        
        // When
        StructuredLogging.logInfo(logger, message, *fields)
        
        // Then
        // MDC가 정리되었는지 확인
        assertTrue(MDC.getCopyOfContextMap()?.isEmpty() ?: true)
    }
    
    @Test
    fun `null values should be handled gracefully`() {
        // Given
        val message = "Test message with null values"
        val fields = arrayOf("key1" to null, "key2" to "value2")
        
        // When
        StructuredLogging.logInfo(logger, message, *fields)
        
        // Then
        val events = listAppender.list
        assertEquals(1, events.size)
        
        val event = events[0]
        assertEquals(message, event.message)
        
        val mdc = event.mdcPropertyMap
        // null 값은 MDC에 설정되지 않아야 함
        assertTrue(!mdc.containsKey("key1"))
        assertEquals("value2", mdc["key2"])
    }
    
    @Test
    fun `logApiRequestStart should log API request start information`() {
        // Given
        val method = "POST"
        val path = "/api/surveys"
        val requestId = "req-123"
        val userId = "user-456"
        val fields = arrayOf("userAgent" to "Mozilla/5.0", "ipAddress" to "192.168.1.1")
        
        // When
        StructuredLogging.logApiRequestStart(logger, method, path, requestId, userId, *fields)
        
        // Then
        val events = listAppender.list
        assertEquals(1, events.size)
        
        val event = events[0]
        assertEquals("API request started", event.message)
        assertEquals(ch.qos.logback.classic.Level.INFO, event.level)
        
        val mdc = event.mdcPropertyMap
        assertEquals(method, mdc["method"])
        assertEquals(path, mdc["path"])
        assertEquals(requestId, mdc["requestId"])
        assertEquals(userId, mdc["userId"])
        assertEquals("Mozilla/5.0", mdc["userAgent"])
        assertEquals("192.168.1.1", mdc["ipAddress"])
    }
    
    @Test
    fun `logApiRequestComplete should log API request completion information`() {
        // Given
        val method = "POST"
        val path = "/api/surveys"
        val requestId = "req-123"
        val statusCode = 201
        val duration = 245L
        val userId = "user-456"
        val fields = arrayOf("responseSize" to 1024)
        
        // When
        StructuredLogging.logApiRequestComplete(logger, method, path, requestId, statusCode, duration, userId, *fields)
        
        // Then
        val events = listAppender.list
        assertEquals(1, events.size)
        
        val event = events[0]
        assertEquals("API request completed", event.message)
        assertEquals(ch.qos.logback.classic.Level.INFO, event.level)
        
        val mdc = event.mdcPropertyMap
        assertEquals(method, mdc["method"])
        assertEquals(path, mdc["path"])
        assertEquals(requestId, mdc["requestId"])
        assertEquals(statusCode.toString(), mdc["status"])
        assertEquals(duration.toString(), mdc["duration"])
        assertEquals(userId, mdc["userId"])
        assertEquals("1024", mdc["responseSize"])
    }
} 
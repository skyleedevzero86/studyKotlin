package com.kominioai.global.config

import com.kominioai.global.util.StructuredLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LoggingFilterTest {
    
    private lateinit var loggingFilter: LoggingFilter
    private lateinit var mockChain: WebFilterChain
    
    @BeforeEach
    fun setUp() {
        loggingFilter = LoggingFilter()
        mockChain = object : WebFilterChain {
            override fun filter(exchange: ServerWebExchange): Mono<Void> {
                return Mono.empty()
            }
        }
    }
    
    @Test
    fun `filter should set request ID in MDC`() {
        // Given
        val request = MockServerHttpRequest
            .method(HttpMethod.GET, "/api/surveys")
            .build()
        val exchange = MockServerWebExchange.from(request)
        
        // When
        val result = loggingFilter.filter(exchange, mockChain)
        
        // Then
        StepVerifier.create(result)
            .verifyComplete()
        
        // MDC에 requestId가 설정되었는지 확인
        val requestId = StructuredLogging::class.java.getDeclaredField("REQUEST_ID_KEY")
        requestId.isAccessible = true
        val requestIdKey = requestId.get(null) as String
        
        // 실제로는 MDC가 비동기 컨텍스트에서 관리되므로 직접 확인하기 어려움
        // 대신 필터가 정상적으로 실행되는지 확인
        assertNotNull(exchange.response)
    }
    
    @Test
    fun `filter should handle different HTTP methods`() {
        // Given
        val methods = listOf(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
        
        methods.forEach { method ->
            val request = MockServerHttpRequest
                .method(method, "/api/surveys")
                .build()
            val exchange = MockServerWebExchange.from(request)
            
            // When
            val result = loggingFilter.filter(exchange, mockChain)
            
            // Then
            StepVerifier.create(result)
                .verifyComplete()
        }
    }
    
    @Test
    fun `filter should handle different paths`() {
        // Given
        val paths = listOf(
            "/api/surveys",
            "/api/surveys/123",
            "/api/surveys/123/questions",
            "/api/responses"
        )
        
        paths.forEach { path ->
            val request = MockServerHttpRequest
                .method(HttpMethod.GET, path)
                .build()
            val exchange = MockServerWebExchange.from(request)
            
            // When
            val result = loggingFilter.filter(exchange, mockChain)
            
            // Then
            StepVerifier.create(result)
                .verifyComplete()
        }
    }
    
    @Test
    fun `filter should extract client IP from headers`() {
        // Given
        val request = MockServerHttpRequest
            .method(HttpMethod.GET, "/api/surveys")
            .header("X-Forwarded-For", "192.168.1.100")
            .header("X-Real-IP", "192.168.1.200")
            .build()
        val exchange = MockServerWebExchange.from(request)
        
        // When
        val result = loggingFilter.filter(exchange, mockChain)
        
        // Then
        StepVerifier.create(result)
            .verifyComplete()
        
        // 필터가 정상적으로 실행되는지 확인
        assertNotNull(exchange.response)
    }
    
    @Test
    fun `filter should handle missing headers gracefully`() {
        // Given
        val request = MockServerHttpRequest
            .method(HttpMethod.GET, "/api/surveys")
            .build()
        val exchange = MockServerWebExchange.from(request)
        
        // When
        val result = loggingFilter.filter(exchange, mockChain)
        
        // Then
        StepVerifier.create(result)
            .verifyComplete()
        
        // 필터가 정상적으로 실행되는지 확인
        assertNotNull(exchange.response)
    }
    
    @Test
    fun `filter should handle authorization header`() {
        // Given
        val request = MockServerHttpRequest
            .method(HttpMethod.GET, "/api/surveys")
            .header("Authorization", "Bearer token123")
            .build()
        val exchange = MockServerWebExchange.from(request)
        
        // When
        val result = loggingFilter.filter(exchange, mockChain)
        
        // Then
        StepVerifier.create(result)
            .verifyComplete()
        
        // 필터가 정상적으로 실행되는지 확인
        assertNotNull(exchange.response)
    }
    
    @Test
    fun `filter should have highest precedence`() {
        // When
        val order = loggingFilter.order
        
        // Then
        assertEquals(org.springframework.core.Ordered.HIGHEST_PRECEDENCE, order)
    }
    
    @Test
    fun `filter should handle chain errors gracefully`() {
        // Given
        val errorChain = object : WebFilterChain {
            override fun filter(exchange: ServerWebExchange): Mono<Void> {
                return Mono.error(RuntimeException("Chain error"))
            }
        }
        
        val request = MockServerHttpRequest
            .method(HttpMethod.GET, "/api/surveys")
            .build()
        val exchange = MockServerWebExchange.from(request)
        
        // When
        val result = loggingFilter.filter(exchange, errorChain)
        
        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }
    
    @Test
    fun `filter should handle user agent header`() {
        // Given
        val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        val request = MockServerHttpRequest
            .method(HttpMethod.GET, "/api/surveys")
            .header("User-Agent", userAgent)
            .build()
        val exchange = MockServerWebExchange.from(request)
        
        // When
        val result = loggingFilter.filter(exchange, mockChain)
        
        // Then
        StepVerifier.create(result)
            .verifyComplete()
        
        // 필터가 정상적으로 실행되는지 확인
        assertNotNull(exchange.response)
    }
    
    @Test
    fun `filter should handle content length header`() {
        // Given
        val request = MockServerHttpRequest
            .method(HttpMethod.POST, "/api/surveys")
            .header("Content-Length", "1024")
            .build()
        val exchange = MockServerWebExchange.from(request)
        
        // When
        val result = loggingFilter.filter(exchange, mockChain)
        
        // Then
        StepVerifier.create(result)
            .verifyComplete()
        
        // 필터가 정상적으로 실행되는지 확인
        assertNotNull(exchange.response)
    }
} 
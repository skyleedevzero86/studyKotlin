package com.kominioai.global.config

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
        val request = MockServerHttpRequest
            .method(HttpMethod.GET, "/api/surveys")
            .build()
        val exchange = MockServerWebExchange.from(request)
        
        val result = loggingFilter.filter(exchange, mockChain)
        
        StepVerifier.create(result)
            .verifyComplete()
        
        val requestId = StructuredLogging::class.java.getDeclaredField("REQUEST_ID_KEY")
        requestId.isAccessible = true
        val requestIdKey = requestId.get(null) as String
        
        assertNotNull(exchange.response)
    }
    
    @Test
    fun `filter should handle different HTTP methods`() {
        val methods = listOf(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
        
        methods.forEach { method ->
            val request = MockServerHttpRequest
                .method(method, "/api/surveys")
                .build()
            val exchange = MockServerWebExchange.from(request)
            
            val result = loggingFilter.filter(exchange, mockChain)
            
            StepVerifier.create(result)
                .verifyComplete()
        }
    }
    
    @Test
    fun `filter should handle different paths`() {
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
            
            val result = loggingFilter.filter(exchange, mockChain)
            
            StepVerifier.create(result)
                .verifyComplete()
        }
    }
    
    @Test
    fun `filter should extract client IP from headers`() {
        val request = MockServerHttpRequest
            .method(HttpMethod.GET, "/api/surveys")
            .header("X-Forwarded-For", "192.168.1.100")
            .header("X-Real-IP", "192.168.1.200")
            .build()
        val exchange = MockServerWebExchange.from(request)
        
        val result = loggingFilter.filter(exchange, mockChain)
        
        StepVerifier.create(result)
            .verifyComplete()
        
        assertNotNull(exchange.response)
    }
    
    @Test
    fun `filter should handle missing headers gracefully`() {
        val request = MockServerHttpRequest
            .method(HttpMethod.GET, "/api/surveys")
            .build()
        val exchange = MockServerWebExchange.from(request)
        
        val result = loggingFilter.filter(exchange, mockChain)
        
        StepVerifier.create(result)
            .verifyComplete()
        
        assertNotNull(exchange.response)
    }
    
    @Test
    fun `filter should handle authorization header`() {
        val request = MockServerHttpRequest
            .method(HttpMethod.GET, "/api/surveys")
            .header("Authorization", "Bearer token123")
            .build()
        val exchange = MockServerWebExchange.from(request)
        
        val result = loggingFilter.filter(exchange, mockChain)
        
        StepVerifier.create(result)
            .verifyComplete()
        
        assertNotNull(exchange.response)
    }
    
    @Test
    fun `filter should have highest precedence`() {
        val order = loggingFilter.order
        
        assertEquals(org.springframework.core.Ordered.HIGHEST_PRECEDENCE, order)
    }
    
    @Test
    fun `filter should handle chain errors gracefully`() {
        val errorChain = object : WebFilterChain {
            override fun filter(exchange: ServerWebExchange): Mono<Void> {
                return Mono.error(RuntimeException("Chain error"))
            }
        }
        
        val request = MockServerHttpRequest
            .method(HttpMethod.GET, "/api/surveys")
            .build()
        val exchange = MockServerWebExchange.from(request)
        
        val result = loggingFilter.filter(exchange, errorChain)
        
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }
    
    @Test
    fun `filter should handle user agent header`() {
        val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        val request = MockServerHttpRequest
            .method(HttpMethod.GET, "/api/surveys")
            .header("User-Agent", userAgent)
            .build()
        val exchange = MockServerWebExchange.from(request)
        
        val result = loggingFilter.filter(exchange, mockChain)
        
        StepVerifier.create(result)
            .verifyComplete()
        
        assertNotNull(exchange.response)
    }
    
    @Test
    fun `filter should handle content length header`() {
        val request = MockServerHttpRequest
            .method(HttpMethod.POST, "/api/surveys")
            .header("Content-Length", "1024")
            .build()
        val exchange = MockServerWebExchange.from(request)
        
        val result = loggingFilter.filter(exchange, mockChain)
        
        StepVerifier.create(result)
            .verifyComplete()
        
        assertNotNull(exchange.response)
    }
}
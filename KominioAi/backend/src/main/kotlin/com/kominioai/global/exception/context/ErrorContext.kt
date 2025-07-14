package com.kominioai.global.exception.context

import org.springframework.web.server.ServerWebExchange
import java.time.Duration
import java.time.Instant

/**
 * 에러 발생 컨텍스트 정보
 */
data class ErrorContext(
    val requestId: String,
    val userId: String?,
    val traceId: String?,
    val spanId: String?,
    val userAgent: String?,
    val clientIp: String?,
    val requestPath: String,
    val requestMethod: String,
    val requestHeaders: Map<String, String>,
    val requestParams: Map<String, String>,
    val startTime: Instant,
    val duration: Duration,
    val environment: String,
    val version: String
) {
    companion object {
        fun fromExchange(exchange: ServerWebExchange, startTime: Instant): ErrorContext {
            val request = exchange.request
            val response = exchange.response
            
            return ErrorContext(
                requestId = exchange.getAttribute("requestId") ?: generateRequestId(),
                userId = exchange.getAttribute("userId"),
                traceId = exchange.getAttribute("traceId"),
                spanId = exchange.getAttribute("spanId"),
                userAgent = request.headers.getFirst("User-Agent"),
                clientIp = getClientIp(request),
                requestPath = request.path.value(),
                requestMethod = request.method?.name ?: "UNKNOWN",
                requestHeaders = request.headers.toSingleValueMap(),
                requestParams = request.queryParams.toSingleValueMap(),
                startTime = startTime,
                duration = Duration.between(startTime, Instant.now()),
                environment = System.getProperty("spring.profiles.active", "default"),
                version = System.getProperty("app.version", "1.0.0")
            )
        }
        
        private fun generateRequestId(): String = java.util.UUID.randomUUID().toString()
        
        private fun getClientIp(request: org.springframework.http.server.reactive.ServerHttpRequest): String? {
            return request.headers.getFirst("X-Forwarded-For")
                ?: request.headers.getFirst("X-Real-IP")
                ?: request.remoteAddress?.address?.hostAddress
        }
    }
} 
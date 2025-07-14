package com.kominioai.global.exception.base

import org.springframework.web.server.ServerWebExchange
import java.time.Duration
import java.time.Instant

data class ErrorContext(
    val requestId: String,
    val userId: String? = null,
    val traceId: String? = null,
    val spanId: String? = null,
    val userAgent: String? = null,
    val clientIp: String? = null,
    val requestPath: String,
    val requestMethod: String,
    val requestHeaders: Map<String, String>? = null,
    val requestParams: Map<String, String>? = null,
    val startTime: Instant = Instant.now(),
    val duration: Duration? = null,
    val environment: String = System.getProperty("spring.profiles.active", "default"),
    val version: String = System.getProperty("app.version", "1.0.0")
) {
    companion object {
        fun fromExchange(exchange: ServerWebExchange, startTime: Instant): ErrorContext {
            val request = exchange.request

            return ErrorContext(
                requestId = exchange.getAttribute("requestId") ?: generateRequestId(),
                userId = exchange.getAttribute("userId"),
                traceId = exchange.getAttribute("traceId"),
                spanId = exchange.getAttribute("spanId"),
                userAgent = request.headers.getFirst("User-Agent"),
                clientIp = getClientIp(request),
                requestPath = request.path.value(),
                requestMethod = getRequestMethod(request),
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

        private fun getRequestMethod(request: org.springframework.http.server.reactive.ServerHttpRequest): String {
            return request.method?.name() ?: "UNKNOWN"
        }
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "requestId" to requestId,
            "userId" to userId,
            "traceId" to traceId,
            "spanId" to spanId,
            "userAgent" to userAgent,
            "clientIp" to clientIp,
            "requestPath" to requestPath,
            "requestMethod" to requestMethod,
            "requestHeaders" to requestHeaders,
            "requestParams" to requestParams,
            "startTime" to startTime,
            "duration" to duration,
            "environment" to environment,
            "version" to version
        )
    }
}
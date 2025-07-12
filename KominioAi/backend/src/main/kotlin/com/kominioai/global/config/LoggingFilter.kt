package com.kominioai.global.config

import com.kominioai.global.util.StructuredLogging
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class LoggingFilter : WebFilter, Ordered {
    
    private val logger = LoggerFactory.getLogger(LoggingFilter::class.java)
    
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val response = exchange.response
        val requestId = StructuredLogging.generateRequestId()
        val startTime = System.currentTimeMillis()

        StructuredLogging.setRequestId(requestId)

        StructuredLogging.logApiRequestStart(
            logger = logger,
            method = request.method?.toString() ?: "UNKNOWN",
            path = request.path.value(),
            requestId = requestId,
            userId = extractUserId(request),
            "userAgent" to request.headers.getFirst("User-Agent"),
            "ipAddress" to extractClientIp(request),
            "timestamp" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
        
        return chain.filter(exchange)
            .doFinally {
                val duration = System.currentTimeMillis() - startTime

                StructuredLogging.logApiRequestComplete(
                    logger = logger,
                    method = request.method?.toString() ?: "UNKNOWN",
                    path = request.path.value(),
                    requestId = requestId,
                    statusCode = response.statusCode?.value() ?: 0,
                    duration = duration,
                    userId = extractUserId(request),
                    "responseSize" to response.headers.contentLength,
                    "timestamp" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )

                StructuredLogging.clearContext()
            }
    }
    
    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }
    
    private fun extractUserId(request: org.springframework.http.server.reactive.ServerHttpRequest): String? {

        val authHeader = request.headers.getFirst("Authorization")
        if (authHeader?.startsWith("Bearer ") == true) {
            return "extracted-user-id"
        }
        return null
    }
    
    private fun extractClientIp(request: org.springframework.http.server.reactive.ServerHttpRequest): String? {
        return request.headers.getFirst("X-Forwarded-For")
            ?: request.headers.getFirst("X-Real-IP")
            ?: request.remoteAddress?.address?.hostAddress
    }
} 
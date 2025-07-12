package com.kominioai.global.config

import com.kominioai.global.util.MetricsUtils
import com.kominioai.global.util.StructuredLogging
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * API 메트릭 수집을 위한 WebFlux 필터
 *
 * @author KominioAI Team
 * @since 1.0.0
 */
@Component
@Order(1)
class MetricsWebFilter(private val metricsUtils: MetricsUtils) : WebFilter {

    private val logger = LoggerFactory.getLogger(MetricsWebFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val startTime = Instant.now()
        val request = exchange.request
        val path = request.path.value()
        val method = request.method?.toString() ?: "UNKNOWN"

        // Actuator 엔드포인트는 메트릭 수집에서 제외
        if (path.startsWith("/actuator")) {
            return chain.filter(exchange)
        }

        return chain.filter(exchange)
            .doFinally {
                val endTime = Instant.now()
                val durationMs = endTime.toEpochMilli() - startTime.toEpochMilli()
                val statusCode = exchange.response.statusCode?.value() ?: 500

                // API 메트릭 기록
                metricsUtils.recordApiResponseTime(path, method, statusCode, durationMs)

                // 구조화된 로깅
                StructuredLogging.logInfo(
                    logger = logger,
                    message = "API Request Completed",
                    "path" to path,
                    "method" to method,
                    "statusCode" to statusCode.toString(),
                    "durationMs" to durationMs.toString(),
                    "userAgent" to (request.headers.getFirst("User-Agent") ?: "unknown")
                )

                // 에러율 모니터링
                if (statusCode >= 400) {
                    StructuredLogging.logError(
                        logger = logger,
                        message = "API Error Detected",
                        throwable = null,
                        "path" to path,
                        "method" to method,
                        "statusCode" to statusCode.toString(),
                        "durationMs" to durationMs.toString()
                    )
                }
            }
    }
}
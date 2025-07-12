package com.kominioai.global.controller

import com.kominioai.domain.survey.application.port.output.EventPublisher
import com.kominioai.domain.survey.application.port.output.PublishStats
import com.kominioai.global.util.StructuredLogging
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.LocalDateTime

/**
 * 이벤트 관리 컨트롤러
 * 이벤트 발행 상태, 통계, 관리 기능을 제공
 */
@RestController
@RequestMapping("/api/admin/events")
@PreAuthorize("hasRole('ADMIN')")
class EventController(
    private val eventPublisher: EventPublisher,
    private val meterRegistry: MeterRegistry
) {
    
    private val logger = LoggerFactory.getLogger(EventController::class.java)
    
    /**
     * 이벤트 발행 상태 확인
     */
    @GetMapping("/health")
    fun getEventPublisherHealth(): Mono<ResponseEntity<Map<String, Any>>> {
        val startTime = System.currentTimeMillis()
        
        StructuredLogging.logInfo(
            logger = logger,
            message = "Event publisher health check started",
            "operation" to "GET_EVENT_PUBLISHER_HEALTH"
        )
        
        return Mono.fromCallable {
            val isHealthy = eventPublisher.isHealthy()
            val duration = System.currentTimeMillis() - startTime
            
            StructuredLogging.logInfo(
                logger = logger,
                message = "Event publisher health check completed",
                "operation" to "GET_EVENT_PUBLISHER_HEALTH",
                "duration" to duration,
                "isHealthy" to isHealthy
            )
            
            mapOf(
                "healthy" to isHealthy,
                "timestamp" to LocalDateTime.now().toString(),
                "duration" to duration
            )
        }.map { ResponseEntity.ok(it) }
    }
    
    /**
     * 이벤트 발행 통계 조회
     */
    @GetMapping("/stats")
    fun getEventPublishStats(): Mono<ResponseEntity<PublishStats>> {
        val startTime = System.currentTimeMillis()
        
        StructuredLogging.logInfo(
            logger = logger,
            message = "Event publish stats retrieval started",
            "operation" to "GET_EVENT_PUBLISH_STATS"
        )
        
        return Mono.fromCallable {
            val stats = eventPublisher.getPublishStats()
            val duration = System.currentTimeMillis() - startTime
            
            StructuredLogging.logInfo(
                logger = logger,
                message = "Event publish stats retrieved successfully",
                "operation" to "GET_EVENT_PUBLISH_STATS",
                "duration" to duration,
                "totalPublished" to stats.totalPublished,
                "successCount" to stats.successCount,
                "failureCount" to stats.failureCount
            )
            
            stats
        }.map { ResponseEntity.ok(it) }
    }
    
    /**
     * 이벤트 메트릭 조회
     */
    @GetMapping("/metrics")
    fun getEventMetrics(): Mono<ResponseEntity<Map<String, Any>>> {
        val startTime = System.currentTimeMillis()
        
        StructuredLogging.logInfo(
            logger = logger,
            message = "Event metrics retrieval started",
            "operation" to "GET_EVENT_METRICS"
        )
        
        return Mono.fromCallable {
            val metrics = mutableMapOf<String, Any>()
            
            // 이벤트 발행 메트릭
            val eventPublishCounter = meterRegistry.counter("event.publish.success")
            val eventPublishFailureCounter = meterRegistry.counter("event.publish.failure")
            val eventPublishTimer = meterRegistry.timer("event.publish.duration")
            
            metrics["eventPublishCount"] = eventPublishCounter.count()
            metrics["eventPublishFailureCount"] = eventPublishFailureCounter.count()
            metrics["eventPublishAverageTime"] = eventPublishTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS)
            metrics["eventPublishMaxTime"] = eventPublishTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS)
            
            // 이벤트 핸들러 메트릭
            val eventHandlerSuccessCounter = meterRegistry.counter("event.handler.success")
            val eventHandlerFailureCounter = meterRegistry.counter("event.handler.failure")
            val eventHandlerTimer = meterRegistry.timer("event.handler.duration")
            
            metrics["eventHandlerSuccessCount"] = eventHandlerSuccessCounter.count()
            metrics["eventHandlerFailureCount"] = eventHandlerFailureCounter.count()
            metrics["eventHandlerAverageTime"] = eventHandlerTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS)
            metrics["eventHandlerMaxTime"] = eventHandlerTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS)
            
            // 이벤트 타입별 메트릭
            val eventTypes = listOf("SurveyCreated", "SurveyPublished", "QuestionAdded", "ResponseSubmitted")
            eventTypes.forEach { eventType ->
                try {
                    val counter = meterRegistry.counter("event.published", "eventType", eventType)
                    metrics["eventType_${eventType}"] = counter.count()
                } catch (e: Exception) {
                    metrics["eventType_${eventType}"] = 0.0
                }
            }
            
            val duration = System.currentTimeMillis() - startTime
            
            StructuredLogging.logInfo(
                logger = logger,
                message = "Event metrics retrieved successfully",
                "operation" to "GET_EVENT_METRICS",
                "duration" to duration,
                "metricsCount" to metrics.size
            )
            
            metrics
        }.map { ResponseEntity.ok(it) }
    }
    
    /**
     * 이벤트 발행 테스트
     */
    @PostMapping("/test")
    fun testEventPublishing(): Mono<ResponseEntity<Map<String, String>>> {
        val startTime = System.currentTimeMillis()
        
        StructuredLogging.logInfo(
            logger = logger,
            message = "Event publishing test started",
            "operation" to "TEST_EVENT_PUBLISHING"
        )
        
        return Mono.fromCallable {
            // 테스트용 이벤트 생성 (실제로는 더 구체적인 이벤트를 사용해야 함)
            val testEvent = object : com.kominioai.domain.survey.domain.model.event.SurveyEvent {
                override val eventId: String = "test-event-${System.currentTimeMillis()}"
                override val eventType: String = "TestEvent"
                override val occurredAt: java.time.Instant = java.time.Instant.now()
                override val version: String = "1.0"
            }
            
            val metadata = com.kominioai.domain.survey.domain.model.event.EventMetadata(
                userId = "test-user",
                source = "test-service",
                tags = mapOf("operation" to "test_event_publishing")
            )
            
            // 이벤트 발행 테스트
            eventPublisher.publishReactive(testEvent, metadata)
                .subscribe(
                    { 
                        StructuredLogging.logInfo(
                            logger = logger,
                            message = "Test event published successfully",
                            "eventId" to testEvent.eventId
                        )
                    },
                    { error ->
                        StructuredLogging.logError(
                            logger = logger,
                            message = "Test event publishing failed",
                            throwable = error,
                            "eventId" to testEvent.eventId
                        )
                    }
                )
            
            val duration = System.currentTimeMillis() - startTime
            
            StructuredLogging.logInfo(
                logger = logger,
                message = "Event publishing test completed successfully",
                "operation" to "TEST_EVENT_PUBLISHING",
                "duration" to duration,
                "eventId" to testEvent.eventId
            )
            
            mapOf(
                "message" to "Test event published successfully",
                "eventId" to testEvent.eventId,
                "duration" to duration.toString()
            )
        }.map { ResponseEntity.ok(it) }
        .onErrorResume { error ->
            val duration = System.currentTimeMillis() - startTime
            
            StructuredLogging.logError(
                logger = logger,
                message = "Event publishing test failed",
                throwable = error,
                "operation" to "TEST_EVENT_PUBLISHING",
                "duration" to duration
            )
            
            Mono.just(ResponseEntity.internalServerError().body(
                mapOf(
                    "message" to "Test event publishing failed: ${error.message}",
                    "duration" to duration.toString()
                )
            ))
        }
    }
    
    /**
     * 이벤트 발행기 재시작 (필요시)
     */
    @PostMapping("/restart")
    fun restartEventPublisher(): Mono<ResponseEntity<Map<String, String>>> {
        StructuredLogging.logWarn(
            logger = logger,
            message = "Event publisher restart requested",
            "operation" to "RESTART_EVENT_PUBLISHER"
        )
        
        return Mono.fromCallable {
            // 실제로는 이벤트 발행기를 재시작하는 로직이 필요
            // 현재는 Spring ApplicationEventPublisher를 사용하므로 재시작이 필요하지 않음
            mapOf(
                "message" to "Event publisher restart not required for Spring ApplicationEventPublisher",
                "timestamp" to LocalDateTime.now().toString()
            )
        }.map { ResponseEntity.ok(it) }
    }
} 
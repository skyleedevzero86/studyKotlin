package com.kominioai.domain.survey.infrastructure.event.listener

import com.kominioai.domain.survey.domain.model.event.SystemEvent
import com.kominioai.global.service.BusinessMetricsService
import com.kominioai.global.util.StructuredLogging
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/**
 * 시스템 이벤트 리스너
 * 캐시 무효화, 성능 알림 등의 시스템 이벤트를 처리
 */
@Component
class SystemEventListener(
    private val businessMetricsService: BusinessMetricsService,
    private val meterRegistry: MeterRegistry
) {
    
    private val logger = LoggerFactory.getLogger(SystemEventListener::class.java)
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleCacheInvalidated(event: SystemEvent.CacheInvalidated) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling CacheInvalidated event - eventId: ${event.eventId}, cacheType: ${event.cacheType}, targetId: ${event.targetId}")
        
        try {
            // 1. 캐시 무효화 메트릭 기록
            meterRegistry.counter("cache.invalidated", "type", event.cacheType).increment()
            
            // 2. 비즈니스 메트릭 기록
            businessMetricsService.recordCacheInvalidated(event.cacheType, event.targetId)
            
            // 3. 캐시 무효화 알림 발송 (필요시)
            sendCacheInvalidatedNotification(event)
            
            // 4. 성공 메트릭 기록
            recordSuccessMetrics("CacheInvalidated", System.currentTimeMillis() - startTime)
            
            logger.info("CacheInvalidated event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("CacheInvalidated", e)
            logger.error("Failed to handle CacheInvalidated event: ${e.message}", e)
            throw e
        }
    }
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handlePerformanceAlert(event: SystemEvent.PerformanceAlert) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling PerformanceAlert event - eventId: ${event.eventId}, alertType: ${event.alertType}, severity: ${event.severity}, message: ${event.message}")
        
        try {
            // 1. 성능 알림 메트릭 기록
            meterRegistry.counter("performance.alert", "type", event.alertType, "severity", event.severity).increment()
            
            // 2. 비즈니스 메트릭 기록
            businessMetricsService.recordPerformanceAlert(event.alertType, event.severity, event.message)
            
            // 3. 심각도에 따른 처리
            handleAlertBySeverity(event)
            
            // 4. 성능 알림 발송
            sendPerformanceAlertNotification(event)
            
            // 5. 성공 메트릭 기록
            recordSuccessMetrics("PerformanceAlert", System.currentTimeMillis() - startTime)
            
            logger.info("PerformanceAlert event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("PerformanceAlert", e)
            logger.error("Failed to handle PerformanceAlert event: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * 심각도에 따른 알림 처리
     */
    private fun handleAlertBySeverity(event: SystemEvent.PerformanceAlert) {
        when (event.severity) {
            "LOW" -> {
                logger.debug("Low severity alert: ${event.message}")
                // 로그만 남기고 추가 처리 없음
            }
            "MEDIUM" -> {
                logger.warn("Medium severity alert: ${event.message}")
                // 경고 로그 및 기본 모니터링
                meterRegistry.counter("performance.alert.medium").increment()
            }
            "HIGH" -> {
                logger.error("High severity alert: ${event.message}")
                // 에러 로그 및 즉시 알림
                meterRegistry.counter("performance.alert.high").increment()
                sendImmediateAlert(event)
            }
            "CRITICAL" -> {
                logger.error("CRITICAL severity alert: ${event.message}")
                // 크리티컬 로그 및 긴급 조치
                meterRegistry.counter("performance.alert.critical").increment()
                sendCriticalAlert(event)
                triggerEmergencyResponse(event)
            }
            else -> {
                logger.warn("Unknown severity level: ${event.severity}")
            }
        }
    }
    
    /**
     * 즉시 알림 발송
     */
    private fun sendImmediateAlert(event: SystemEvent.PerformanceAlert) {
        // TODO: 즉시 알림 발송 로직 구현 (Slack, 이메일 등)
        logger.warn("Sending immediate alert for high severity issue: ${event.message}")
    }
    
    /**
     * 크리티컬 알림 발송
     */
    private fun sendCriticalAlert(event: SystemEvent.PerformanceAlert) {
        // TODO: 크리티컬 알림 발송 로직 구현 (SMS, 전화 등)
        logger.error("Sending critical alert: ${event.message}")
    }
    
    /**
     * 긴급 대응 트리거
     */
    private fun triggerEmergencyResponse(event: SystemEvent.PerformanceAlert) {
        // TODO: 긴급 대응 로직 구현 (자동 스케일링, 서비스 재시작 등)
        logger.error("Triggering emergency response for: ${event.message}")
    }
    
    /**
     * 캐시 무효화 알림 발송
     */
    private fun sendCacheInvalidatedNotification(event: SystemEvent.CacheInvalidated) {
        // TODO: 캐시 무효화 알림 발송 로직 구현
        logger.debug("Sending cache invalidated notification for type: ${event.cacheType}")
    }
    
    /**
     * 성능 알림 발송
     */
    private fun sendPerformanceAlertNotification(event: SystemEvent.PerformanceAlert) {
        // TODO: 성능 알림 발송 로직 구현
        logger.debug("Sending performance alert notification: ${event.message}")
    }
    
    /**
     * 성공 메트릭 기록
     */
    private fun recordSuccessMetrics(eventType: String, duration: Long) {
        meterRegistry.counter("event.handler.success", "type", eventType).increment()
        meterRegistry.timer("event.handler.duration", "type", eventType).record(java.time.Duration.ofMillis(duration))
    }
    
    /**
     * 실패 메트릭 기록
     */
    private fun recordFailureMetrics(eventType: String, error: Exception) {
        meterRegistry.counter("event.handler.failure", "type", eventType, "error", error.javaClass.simpleName).increment()
    }
} 
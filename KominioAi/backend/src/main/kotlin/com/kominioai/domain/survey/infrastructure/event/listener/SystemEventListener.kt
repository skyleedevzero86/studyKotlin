package com.kominioai.domain.survey.infrastructure.event.listener

import com.kominioai.domain.survey.domain.model.event.SystemEvent
import com.kominioai.global.service.BusinessMetricsService
import com.kominioai.global.util.StructuredLogging
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

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

            meterRegistry.counter("cache.invalidated", "type", event.cacheType).increment()

            businessMetricsService.recordCacheInvalidated(event.cacheType, event.targetId)

            sendCacheInvalidatedNotification(event)

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

            meterRegistry.counter("performance.alert", "type", event.alertType, "severity", event.severity).increment()

            businessMetricsService.recordPerformanceAlert(event.alertType, event.severity, event.message)

            handleAlertBySeverity(event)

            sendPerformanceAlertNotification(event)

            recordSuccessMetrics("PerformanceAlert", System.currentTimeMillis() - startTime)
            
            logger.info("PerformanceAlert event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("PerformanceAlert", e)
            logger.error("Failed to handle PerformanceAlert event: ${e.message}", e)
            throw e
        }
    }

    private fun handleAlertBySeverity(event: SystemEvent.PerformanceAlert) {
        when (event.severity) {
            "LOW" -> {
                logger.debug("Low severity alert: ${event.message}")
            }
            "MEDIUM" -> {
                logger.warn("Medium severity alert: ${event.message}")
                meterRegistry.counter("performance.alert.medium").increment()
            }
            "HIGH" -> {
                logger.error("High severity alert: ${event.message}")
                meterRegistry.counter("performance.alert.high").increment()
                sendImmediateAlert(event)
            }
            "CRITICAL" -> {
                logger.error("CRITICAL severity alert: ${event.message}")
                meterRegistry.counter("performance.alert.critical").increment()
                sendCriticalAlert(event)
                triggerEmergencyResponse(event)
            }
            else -> {
                logger.warn("Unknown severity level: ${event.severity}")
            }
        }
    }

    private fun sendImmediateAlert(event: SystemEvent.PerformanceAlert) {
        logger.warn("Sending immediate alert for high severity issue: ${event.message}")
    }

    private fun sendCriticalAlert(event: SystemEvent.PerformanceAlert) {

        logger.error("Sending critical alert: ${event.message}")
    }

    private fun triggerEmergencyResponse(event: SystemEvent.PerformanceAlert) {

        logger.error("Triggering emergency response for: ${event.message}")
    }

    private fun sendCacheInvalidatedNotification(event: SystemEvent.CacheInvalidated) {

        logger.debug("Sending cache invalidated notification for type: ${event.cacheType}")
    }

    private fun sendPerformanceAlertNotification(event: SystemEvent.PerformanceAlert) {

        logger.debug("Sending performance alert notification: ${event.message}")
    }

    private fun recordSuccessMetrics(eventType: String, duration: Long) {
        meterRegistry.counter("event.handler.success", "type", eventType).increment()
        meterRegistry.timer("event.handler.duration", "type", eventType).record(java.time.Duration.ofMillis(duration))
    }

    private fun recordFailureMetrics(eventType: String, error: Exception) {
        meterRegistry.counter("event.handler.failure", "type", eventType, "error", error.javaClass.simpleName).increment()
    }
} 
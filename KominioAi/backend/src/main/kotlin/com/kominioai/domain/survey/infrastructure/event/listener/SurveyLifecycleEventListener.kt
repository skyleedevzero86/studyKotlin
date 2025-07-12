package com.kominioai.domain.survey.infrastructure.event.listener

import com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent
import com.kominioai.domain.survey.infrastructure.cache.SurveyCacheService
import com.kominioai.global.service.BusinessMetricsService
import com.kominioai.global.util.StructuredLogging
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SurveyLifecycleEventListener(
    private val surveyCacheService: SurveyCacheService,
    private val businessMetricsService: BusinessMetricsService,
    private val meterRegistry: MeterRegistry
) {
    
    private val logger = LoggerFactory.getLogger(SurveyLifecycleEventListener::class.java)
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleSurveyCreated(event: SurveyLifecycleEvent.SurveyCreated) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling SurveyCreated event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, title: ${event.title}, createdBy: ${event.createdBy.value}")
        
        try {
            surveyCacheService.invalidatePublishedSurveysCache()

            businessMetricsService.recordSurveyCreated(event.surveyId.value)

            sendSurveyCreatedNotification(event)

            recordSuccessMetrics("SurveyCreated", System.currentTimeMillis() - startTime)
            
            logger.info("SurveyCreated event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("SurveyCreated", e)
            logger.error("Failed to handle SurveyCreated event: ${e.message}", e)
            throw e
        }
    }
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleSurveyPublished(event: SurveyLifecycleEvent.SurveyPublished) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling SurveyPublished event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, publishedBy: ${event.publishedBy.value}, questionCount: ${event.questionCount}")
        
        try {
            surveyCacheService.invalidatePublishedSurveysCache()
            surveyCacheService.invalidateSurveyCache(event.surveyId)

            businessMetricsService.recordSurveyPublished(event.surveyId.value, event.questionCount)

            sendSurveyPublishedNotification(event)

            notifyExternalSystems(event)

            recordSuccessMetrics("SurveyPublished", System.currentTimeMillis() - startTime)
            
            logger.info("SurveyPublished event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("SurveyPublished", e)
            logger.error("Failed to handle SurveyPublished event: ${e.message}", e)
            throw e
        }
    }
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleSurveyClosed(event: SurveyLifecycleEvent.SurveyClosed) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling SurveyClosed event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, closedBy: ${event.closedBy.value}, reason: ${event.reason}")
        
        try {
            surveyCacheService.invalidatePublishedSurveysCache()
            surveyCacheService.invalidateSurveyCache(event.surveyId)

            businessMetricsService.recordSurveyClosed(event.surveyId.value, event.reason)

            sendSurveyClosedNotification(event)

            recordSuccessMetrics("SurveyClosed", System.currentTimeMillis() - startTime)
            
            logger.info("SurveyClosed event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("SurveyClosed", e)
            logger.error("Failed to handle SurveyClosed event: ${e.message}", e)
            throw e
        }
    }
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleSurveyDeleted(event: SurveyLifecycleEvent.SurveyDeleted) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling SurveyDeleted event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, deletedBy: ${event.deletedBy.value}")
        
        try {
            surveyCacheService.invalidateAllSurveyCache()

            businessMetricsService.recordSurveyDeleted(event.surveyId.value)

            cleanupSurveyData(event.surveyId.value)

            recordSuccessMetrics("SurveyDeleted", System.currentTimeMillis() - startTime)
            
            logger.info("SurveyDeleted event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("SurveyDeleted", e)
            logger.error("Failed to handle SurveyDeleted event: ${e.message}", e)
            throw e
        }
    }

    private fun sendSurveyCreatedNotification(event: SurveyLifecycleEvent.SurveyCreated) {
        logger.debug("Sending survey created notification for survey: ${event.surveyId.value}")
    }

    private fun sendSurveyPublishedNotification(event: SurveyLifecycleEvent.SurveyPublished) {
        logger.debug("Sending survey published notification for survey: ${event.surveyId.value}")
    }

    private fun sendSurveyClosedNotification(event: SurveyLifecycleEvent.SurveyClosed) {

        logger.debug("Sending survey closed notification for survey: ${event.surveyId.value}")
    }

    private fun notifyExternalSystems(event: SurveyLifecycleEvent.SurveyPublished) {

        logger.debug("Notifying external systems about survey published: ${event.surveyId.value}")
    }

    private fun cleanupSurveyData(surveyId: String) {

        logger.debug("Cleaning up data for deleted survey: $surveyId")
    }

    private fun recordSuccessMetrics(eventType: String, duration: Long) {
        meterRegistry.counter("event.handler.success", "type", eventType).increment()
        meterRegistry.timer("event.handler.duration", "type", eventType).record(java.time.Duration.ofMillis(duration))
    }

    private fun recordFailureMetrics(eventType: String, error: Exception) {
        meterRegistry.counter("event.handler.failure", "type", eventType, "error", error.javaClass.simpleName).increment()
    }
} 
package com.kominioai.domain.survey.infrastructure.event.listener

import com.kominioai.domain.survey.domain.model.event.ResponseEvent
import com.kominioai.domain.survey.infrastructure.cache.SurveyCacheService
import com.kominioai.global.service.BusinessMetricsService
import com.kominioai.global.util.StructuredLogging
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class ResponseEventListener(
    private val surveyCacheService: SurveyCacheService,
    private val businessMetricsService: BusinessMetricsService,
    private val meterRegistry: MeterRegistry
) {
    
    private val logger = LoggerFactory.getLogger(ResponseEventListener::class.java)
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleResponseSubmitted(event: ResponseEvent.ResponseSubmitted) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling ResponseSubmitted event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, responseId: ${event.responseId.value}, respondentId: ${event.respondentId?.value}, answerCount: ${event.answerCount}")
        
        try {

            surveyCacheService.invalidateSurveyCache(event.surveyId)

            businessMetricsService.recordSurveyResponseSubmitted(
                event.surveyId.value,
                event.answerCount
            )

            sendResponseSubmittedNotification(event)

            recordSuccessMetrics("ResponseSubmitted", System.currentTimeMillis() - startTime)
            
            logger.info("ResponseSubmitted event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("ResponseSubmitted", e)
            logger.error("Failed to handle ResponseSubmitted event: ${e.message}", e)
            throw e
        }
    }
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleResponseCompleted(event: ResponseEvent.ResponseCompleted) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling ResponseCompleted event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, responseId: ${event.responseId.value}, respondentId: ${event.respondentId?.value}, completionTime: ${event.completionTime}")
        
        try {
            analyzeCompletionTime(event)

            businessMetricsService.recordSurveyResponseCompleted(
                event.surveyId.value,
                event.completionTime
            )

            sendResponseCompletedNotification(event)

            checkSurveyMilestones(event.surveyId.value)

            recordSuccessMetrics("ResponseCompleted", System.currentTimeMillis() - startTime)
            
            logger.info("ResponseCompleted event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("ResponseCompleted", e)
            logger.error("Failed to handle ResponseCompleted event: ${e.message}", e)
            throw e
        }
    }
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleResponseDeleted(event: ResponseEvent.ResponseDeleted) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling ResponseDeleted event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, responseId: ${event.responseId.value}, deletedBy: ${event.deletedBy.value}")
        
        try {
            surveyCacheService.invalidateSurveyCache(event.surveyId)

            businessMetricsService.recordSurveyResponseDeleted(event.surveyId.value)

            sendResponseDeletedNotification(event)

            recordSuccessMetrics("ResponseDeleted", System.currentTimeMillis() - startTime)
            
            logger.info("ResponseDeleted event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("ResponseDeleted", e)
            logger.error("Failed to handle ResponseDeleted event: ${e.message}", e)
            throw e
        }
    }

    private fun analyzeCompletionTime(event: ResponseEvent.ResponseCompleted) {
        val completionTimeMs = event.completionTime

        val timeCategory = when {
            completionTimeMs < 30000 -> "FAST" // 30초 미만
            completionTimeMs < 120000 -> "NORMAL" // 2분 미만
            completionTimeMs < 300000 -> "SLOW" // 5분 미만
            else -> "VERY_SLOW" // 5분 이상
        }

        meterRegistry.counter("survey.response.completion.time.category", "category", timeCategory).increment()
        meterRegistry.timer("survey.response.completion.time").record(java.time.Duration.ofMillis(completionTimeMs))
        
        logger.debug("Response completion time analyzed: ${completionTimeMs}ms (${timeCategory})")
    }

    private fun checkSurveyMilestones(surveyId: String) {
        logger.debug("Checking milestones for survey: $surveyId")
    }

    private fun sendResponseSubmittedNotification(event: ResponseEvent.ResponseSubmitted) {
        logger.debug("Sending response submitted notification for survey: ${event.surveyId.value}")
    }

    private fun sendResponseCompletedNotification(event: ResponseEvent.ResponseCompleted) {

        logger.debug("Sending response completed notification for survey: ${event.surveyId.value}")
    }

    private fun sendResponseDeletedNotification(event: ResponseEvent.ResponseDeleted) {

        logger.debug("Sending response deleted notification for survey: ${event.surveyId.value}")
    }

    private fun recordSuccessMetrics(eventType: String, duration: Long) {
        meterRegistry.counter("event.handler.success", "type", eventType).increment()
        meterRegistry.timer("event.handler.duration", "type", eventType).record(java.time.Duration.ofMillis(duration))
    }

    private fun recordFailureMetrics(eventType: String, error: Exception) {
        meterRegistry.counter("event.handler.failure", "type", eventType, "error", error.javaClass.simpleName).increment()
    }
} 
package com.kominioai.domain.survey.infrastructure.event.listener

import com.kominioai.domain.survey.domain.model.event.QuestionEvent
import com.kominioai.domain.survey.infrastructure.cache.SurveyCacheService
import com.kominioai.global.service.BusinessMetricsService
import com.kominioai.global.util.StructuredLogging
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class QuestionEventListener(
    private val surveyCacheService: SurveyCacheService,
    private val businessMetricsService: BusinessMetricsService,
    private val meterRegistry: MeterRegistry
) {
    
    private val logger = LoggerFactory.getLogger(QuestionEventListener::class.java)
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleQuestionAdded(event: QuestionEvent.QuestionAdded) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling QuestionAdded event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, questionId: ${event.questionId.value}, questionType: ${event.questionType}, order: ${event.order}, addedBy: ${event.addedBy.value}")
        
        try {

            surveyCacheService.invalidateSurveyCache(event.surveyId)
            surveyCacheService.invalidateSurveyCache(event.surveyId)

            businessMetricsService.recordQuestionAdded(
                event.surveyId.value,
                event.questionType,
                event.order
            )

            sendQuestionAddedNotification(event)

            recordSuccessMetrics("QuestionAdded", System.currentTimeMillis() - startTime)
            
            logger.info("QuestionAdded event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("QuestionAdded", e)
            logger.error("Failed to handle QuestionAdded event: ${e.message}", e)
            throw e
        }
    }
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleQuestionUpdated(event: QuestionEvent.QuestionUpdated) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling QuestionUpdated event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, questionId: ${event.questionId.value}, updatedBy: ${event.updatedBy.value}, changes: ${event.changes.keys}")
        
        try {

            surveyCacheService.invalidateSurveyCache(event.surveyId)
            surveyCacheService.invalidateSurveyCache(event.surveyId) // Using the same method for now

            businessMetricsService.recordQuestionUpdated(
                event.surveyId.value,
                event.changes.keys.toList()
            )

            sendQuestionUpdatedNotification(event)

            analyzeQuestionChanges(event)

            recordSuccessMetrics("QuestionUpdated", System.currentTimeMillis() - startTime)
            
            logger.info("QuestionUpdated event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("QuestionUpdated", e)
            logger.error("Failed to handle QuestionUpdated event: ${e.message}", e)
            throw e
        }
    }
    
    @EventListener
    @Async("eventListenerTaskExecutor")
    fun handleQuestionDeleted(event: QuestionEvent.QuestionDeleted) {
        val startTime = System.currentTimeMillis()
        
        logger.info("Handling QuestionDeleted event - eventId: ${event.eventId}, surveyId: ${event.surveyId.value}, questionId: ${event.questionId.value}, deletedBy: ${event.deletedBy.value}")
        
        try {

            surveyCacheService.invalidateSurveyCache(event.surveyId)
            surveyCacheService.invalidateSurveyCache(event.surveyId) // Using the same method for now

            businessMetricsService.recordQuestionDeleted(event.surveyId.value)

            sendQuestionDeletedNotification(event)

            recordSuccessMetrics("QuestionDeleted", System.currentTimeMillis() - startTime)
            
            logger.info("QuestionDeleted event handled successfully")
            
        } catch (e: Exception) {
            recordFailureMetrics("QuestionDeleted", e)
            logger.error("Failed to handle QuestionDeleted event: ${e.message}", e)
            throw e
        }
    }

    private fun analyzeQuestionChanges(event: QuestionEvent.QuestionUpdated) {
        val changes = event.changes

        changes.forEach { (field, _) ->
            meterRegistry.counter("question.update.field", "field", field).increment()
        }

        if (changes.containsKey("questionType")) {
            meterRegistry.counter("question.update.type_change").increment()
            logger.warn("Question type changed for question: ${event.questionId.value}")
        }

        if (changes.containsKey("order")) {
            meterRegistry.counter("question.update.order_change").increment()
        }
        
        logger.debug("Question changes analyzed: ${changes.keys}")
    }

    private fun sendQuestionAddedNotification(event: QuestionEvent.QuestionAdded) {

        logger.debug("Sending question added notification for survey: ${event.surveyId.value}")
    }

    private fun sendQuestionUpdatedNotification(event: QuestionEvent.QuestionUpdated) {

        logger.debug("Sending question updated notification for survey: ${event.surveyId.value}")
    }

    private fun sendQuestionDeletedNotification(event: QuestionEvent.QuestionDeleted) {

        logger.debug("Sending question deleted notification for survey: ${event.surveyId.value}")
    }

    private fun recordSuccessMetrics(eventType: String, duration: Long) {
        meterRegistry.counter("event.handler.success", "type", eventType).increment()
        meterRegistry.timer("event.handler.duration", "type", eventType).record(java.time.Duration.ofMillis(duration))
    }

    private fun recordFailureMetrics(eventType: String, error: Exception) {
        meterRegistry.counter("event.handler.failure", "type", eventType, "error", error.javaClass.simpleName).increment()
    }
} 
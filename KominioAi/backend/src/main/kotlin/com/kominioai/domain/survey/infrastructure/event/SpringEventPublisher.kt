package com.kominioai.domain.survey.infrastructure.event

import com.kominioai.domain.survey.application.port.output.EventPublisher
import com.kominioai.domain.survey.application.port.output.PublishStats
import com.kominioai.domain.survey.domain.model.event.SurveyEvent
import com.kominioai.domain.survey.domain.model.event.EventMetadata
import com.kominioai.global.service.BusinessMetricsService
import com.kominioai.global.util.StructuredLogging
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.measureTimeMillis

@Component
class SpringEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val meterRegistry: MeterRegistry,
    private val businessMetricsService: BusinessMetricsService
) : EventPublisher {

    private val logger = LoggerFactory.getLogger(SpringEventPublisher::class.java)

    private val totalPublished = AtomicLong(0)
    private val successCount = AtomicLong(0)
    private val failureCount = AtomicLong(0)
    private val lastPublishTime = AtomicReference<Long>(0L)
    private val totalPublishTime = AtomicLong(0)

    override suspend fun publish(event: SurveyEvent) {
        publish(event, EventMetadata())
    }

    override suspend fun publish(event: SurveyEvent, metadata: EventMetadata) {
        val startTime = System.currentTimeMillis()
        
        try {
            val surveyIdValue = when (event) {
                is com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyCreated -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyUpdated -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyPublished -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyClosed -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyDeleted -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.QuestionEvent.QuestionAdded -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.QuestionEvent.QuestionUpdated -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.QuestionEvent.QuestionDeleted -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.ResponseEvent.ResponseSubmitted -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.ResponseEvent.ResponseCompleted -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.ResponseEvent.ResponseDeleted -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.AnalyticsEvent.SurveyStatisticsUpdated -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.AnalyticsEvent.SurveyMilestoneReached -> event.surveyId.value
                else -> "unknown"
            }
            logger.info("Publishing event: ${event.eventType} - eventId: ${event.eventId}, surveyId: $surveyIdValue, correlationId: ${metadata.correlationId}")

            applicationEventPublisher.publishEvent(event)

            updateMetrics(true, System.currentTimeMillis() - startTime)

            businessMetricsService.recordEventPublished(event.eventType, event.eventId)
            
            logger.info("Event published successfully: ${event.eventType}")
        } catch (e: Exception) {
            updateMetrics(false, System.currentTimeMillis() - startTime)
            logger.error("Failed to publish event: ${event.eventType}, error: ${e.message}", e)
            throw e
        }
    }

    override fun publishReactive(event: SurveyEvent): Mono<Void> {
        return publishReactive(event, EventMetadata())
    }

    override fun publishReactive(event: SurveyEvent, metadata: EventMetadata): Mono<Void> {
        return Mono.fromCallable {
            val startTime = System.currentTimeMillis()
            
            val surveyIdValue = when (event) {
                is com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyCreated -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyUpdated -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyPublished -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyClosed -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent.SurveyDeleted -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.QuestionEvent.QuestionAdded -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.QuestionEvent.QuestionUpdated -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.QuestionEvent.QuestionDeleted -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.ResponseEvent.ResponseSubmitted -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.ResponseEvent.ResponseCompleted -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.ResponseEvent.ResponseDeleted -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.AnalyticsEvent.SurveyStatisticsUpdated -> event.surveyId.value
                is com.kominioai.domain.survey.domain.model.event.AnalyticsEvent.SurveyMilestoneReached -> event.surveyId.value
                else -> "unknown"
            }
            logger.info("Publishing event reactively: ${event.eventType} - eventId: ${event.eventId}, surveyId: $surveyIdValue, correlationId: ${metadata.correlationId}")
            
            applicationEventPublisher.publishEvent(event)
            
            updateMetrics(true, System.currentTimeMillis() - startTime)
            businessMetricsService.recordEventPublished(event.eventType, event.eventId)
            
            logger.info("Event published reactively: ${event.eventType}")
        }.then()
        .onErrorResume { error ->
            updateMetrics(false, 0)
            logger.error("Failed to publish event reactively: ${event.eventType}, error: ${error.message}", error)
            Mono.error(error)
        }
    }

    override suspend fun publishBatch(events: List<SurveyEvent>) {
        val startTime = System.currentTimeMillis()
        
        try {
            val eventTypes = events.map { it.eventType }.distinct()
            logger.info("Publishing batch of ${events.size} events - eventTypes: $eventTypes")
            
            events.forEach { event ->
                applicationEventPublisher.publishEvent(event)
            }
            
            updateMetrics(true, System.currentTimeMillis() - startTime)
            businessMetricsService.recordBatchEventsPublished(events.size)
            
            logger.info("Batch published successfully: ${events.size} events")
        } catch (e: Exception) {
            updateMetrics(false, System.currentTimeMillis() - startTime)
            logger.error("Failed to publish batch events, error: ${e.message}", e)
            throw e
        }
    }

    override fun publishBatchReactive(events: List<SurveyEvent>): Mono<Void> {
        return Mono.fromCallable {
            val startTime = System.currentTimeMillis()
            
            val eventTypes = events.map { it.eventType }.distinct()
            logger.info("Publishing batch reactively: ${events.size} events - eventTypes: $eventTypes")
            
            events.forEach { event ->
                applicationEventPublisher.publishEvent(event)
            }
            
            updateMetrics(true, System.currentTimeMillis() - startTime)
            businessMetricsService.recordBatchEventsPublished(events.size)
            
            logger.info("Batch published reactively: ${events.size} events")
        }.then()
        .onErrorResume { error ->
            updateMetrics(false, 0)
            logger.error("Failed to publish batch events reactively, error: ${error.message}", error)
            Mono.error(error)
        }
    }

    override fun isHealthy(): Boolean {
        return true
    }

    override fun getPublishStats(): PublishStats {
        val total = totalPublished.get()
        val success = successCount.get()
        val failure = failureCount.get()
        val lastPublish = lastPublishTime.get()
        
        val averageTime = if (total > 0) {
            totalPublishTime.get().toDouble() / total
        } else {
            0.0
        }
        
        return PublishStats(
            totalPublished = total,
            successCount = success,
            failureCount = failure,
            averagePublishTime = averageTime,
            lastPublishTime = lastPublish
        )
    }

    private fun updateMetrics(success: Boolean, duration: Long) {
        totalPublished.incrementAndGet()
        
        if (success) {
            successCount.incrementAndGet()
            meterRegistry.counter("event.publish.success", "type", "spring").increment()
        } else {
            failureCount.incrementAndGet()
            meterRegistry.counter("event.publish.failure", "type", "spring").increment()
        }
        
        lastPublishTime.set(System.currentTimeMillis())
        totalPublishTime.addAndGet(duration)

        meterRegistry.timer("event.publish.duration", "type", "spring").record(java.time.Duration.ofMillis(duration))
    }
}
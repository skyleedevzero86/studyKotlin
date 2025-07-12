package com.kominioai.domain.survey.infrastructure.event

import com.kominioai.domain.survey.application.port.output.EventPublisher
import com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent
import com.kominioai.domain.survey.domain.model.event.EventMetadata
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.global.service.BusinessMetricsService
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.context.ApplicationEventPublisher
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

class SpringEventPublisherTest {

    private lateinit var applicationEventPublisher: ApplicationEventPublisher
    private lateinit var meterRegistry: MeterRegistry
    private lateinit var businessMetricsService: BusinessMetricsService
    private lateinit var eventPublisher: SpringEventPublisher

    @BeforeEach
    fun setUp() {
        applicationEventPublisher = mock()
        meterRegistry = SimpleMeterRegistry()
        businessMetricsService = mock()
        eventPublisher = SpringEventPublisher(applicationEventPublisher, meterRegistry, businessMetricsService)
    }

    @Test
    fun `should publish event successfully`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyCreated(
            surveyId = SurveyId.from("test-survey-id"),
            title = "Test Survey",
            description = "Test Description",
            createdBy = UserId.from("test-user-id"),
            settings = mapOf("allowAnonymous" to true)
        )
        
        whenever(applicationEventPublisher.publishEvent(any())).thenReturn(Unit)
        whenever(businessMetricsService.recordEventPublished(any(), any())).thenReturn(Unit)

        // When
        eventPublisher.publish(event)

        // Then
        verify(applicationEventPublisher).publishEvent(event)
        verify(businessMetricsService).recordEventPublished(event.eventType, event.eventId)
        
        // 메트릭 검증
        val successCounter = meterRegistry.get("event.publish.success").counter()
        assert(successCounter.count() == 1.0)
    }

    @Test
    fun `should publish event with metadata successfully`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyPublished(
            surveyId = SurveyId.from("test-survey-id"),
            publishedBy = UserId.from("test-user-id"),
            questionCount = 5
        )
        
        val metadata = EventMetadata(
            userId = "test-user-id",
            source = "test-service",
            tags = mapOf("operation" to "publish_survey")
        )
        
        whenever(applicationEventPublisher.publishEvent(any())).thenReturn(Unit)
        whenever(businessMetricsService.recordEventPublished(any(), any())).thenReturn(Unit)

        // When
        eventPublisher.publish(event, metadata)

        // Then
        verify(applicationEventPublisher).publishEvent(event)
        verify(businessMetricsService).recordEventPublished(event.eventType, event.eventId)
    }

    @Test
    fun `should publish event reactively successfully`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyCreated(
            surveyId = SurveyId.from("test-survey-id"),
            title = "Test Survey",
            description = "Test Description",
            createdBy = UserId.from("test-user-id"),
            settings = mapOf("allowAnonymous" to true)
        )
        
        whenever(applicationEventPublisher.publishEvent(any())).thenReturn(Unit)
        whenever(businessMetricsService.recordEventPublished(any(), any())).thenReturn(Unit)

        // When & Then
        StepVerifier.create(eventPublisher.publishReactive(event))
            .verifyComplete()
        
        verify(applicationEventPublisher).publishEvent(event)
        verify(businessMetricsService).recordEventPublished(event.eventType, event.eventId)
    }

    @Test
    fun `should publish event reactively with metadata successfully`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyPublished(
            surveyId = SurveyId.from("test-survey-id"),
            publishedBy = UserId.from("test-user-id"),
            questionCount = 5
        )
        
        val metadata = EventMetadata(
            userId = "test-user-id",
            source = "test-service",
            tags = mapOf("operation" to "publish_survey")
        )
        
        whenever(applicationEventPublisher.publishEvent(any())).thenReturn(Unit)
        whenever(businessMetricsService.recordEventPublished(any(), any())).thenReturn(Unit)

        // When & Then
        StepVerifier.create(eventPublisher.publishReactive(event, metadata))
            .verifyComplete()
        
        verify(applicationEventPublisher).publishEvent(event)
        verify(businessMetricsService).recordEventPublished(event.eventType, event.eventId)
    }

    @Test
    fun `should handle publish error and record failure metrics`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyCreated(
            surveyId = SurveyId.from("test-survey-id"),
            title = "Test Survey",
            description = "Test Description",
            createdBy = UserId.from("test-user-id"),
            settings = mapOf("allowAnonymous" to true)
        )
        
        val error = RuntimeException("Publish failed")
        whenever(applicationEventPublisher.publishEvent(any())).thenThrow(error)

        // When & Then
        assertThrows<RuntimeException> {
            eventPublisher.publish(event)
        }
        
        // 실패 메트릭 검증
        val failureCounter = meterRegistry.get("event.publish.failure").counter()
        assert(failureCounter.count() == 1.0)
    }

    @Test
    fun `should handle reactive publish error`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyCreated(
            surveyId = SurveyId.from("test-survey-id"),
            title = "Test Survey",
            description = "Test Description",
            createdBy = UserId.from("test-user-id"),
            settings = mapOf("allowAnonymous" to true)
        )
        
        val error = RuntimeException("Publish failed")
        whenever(applicationEventPublisher.publishEvent(any())).thenThrow(error)

        // When & Then
        StepVerifier.create(eventPublisher.publishReactive(event))
            .verifyError(RuntimeException::class.java)
        
        // 실패 메트릭 검증
        val failureCounter = meterRegistry.get("event.publish.failure").counter()
        assert(failureCounter.count() == 1.0)
    }

    @Test
    fun `should publish batch events successfully`() {
        // Given
        val events = listOf(
            SurveyLifecycleEvent.SurveyCreated(
                surveyId = SurveyId.from("test-survey-1"),
                title = "Test Survey 1",
                description = "Test Description 1",
                createdBy = UserId.from("test-user-id"),
                settings = mapOf("allowAnonymous" to true)
            ),
            SurveyLifecycleEvent.SurveyCreated(
                surveyId = SurveyId.from("test-survey-2"),
                title = "Test Survey 2",
                description = "Test Description 2",
                createdBy = UserId.from("test-user-id"),
                settings = mapOf("allowAnonymous" to true)
            )
        )
        
        whenever(applicationEventPublisher.publishEvent(any())).thenReturn(Unit)
        whenever(businessMetricsService.recordBatchEventsPublished(any())).thenReturn(Unit)

        // When
        eventPublisher.publishBatch(events)

        // Then
        verify(applicationEventPublisher, times(2)).publishEvent(any())
        verify(businessMetricsService).recordBatchEventsPublished(2)
    }

    @Test
    fun `should publish batch events reactively successfully`() {
        // Given
        val events = listOf(
            SurveyLifecycleEvent.SurveyCreated(
                surveyId = SurveyId.from("test-survey-1"),
                title = "Test Survey 1",
                description = "Test Description 1",
                createdBy = UserId.from("test-user-id"),
                settings = mapOf("allowAnonymous" to true)
            ),
            SurveyLifecycleEvent.SurveyCreated(
                surveyId = SurveyId.from("test-survey-2"),
                title = "Test Survey 2",
                description = "Test Description 2",
                createdBy = UserId.from("test-user-id"),
                settings = mapOf("allowAnonymous" to true)
            )
        )
        
        whenever(applicationEventPublisher.publishEvent(any())).thenReturn(Unit)
        whenever(businessMetricsService.recordBatchEventsPublished(any())).thenReturn(Unit)

        // When & Then
        StepVerifier.create(eventPublisher.publishBatchReactive(events))
            .verifyComplete()
        
        verify(applicationEventPublisher, times(2)).publishEvent(any())
        verify(businessMetricsService).recordBatchEventsPublished(2)
    }

    @Test
    fun `should return healthy status`() {
        // When
        val isHealthy = eventPublisher.isHealthy()

        // Then
        assert(isHealthy)
    }

    @Test
    fun `should return publish stats`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyCreated(
            surveyId = SurveyId.from("test-survey-id"),
            title = "Test Survey",
            description = "Test Description",
            createdBy = UserId.from("test-user-id"),
            settings = mapOf("allowAnonymous" to true)
        )
        
        whenever(applicationEventPublisher.publishEvent(any())).thenReturn(Unit)
        whenever(businessMetricsService.recordEventPublished(any(), any())).thenReturn(Unit)

        // When
        eventPublisher.publish(event)
        val stats = eventPublisher.getPublishStats()

        // Then
        assert(stats.totalPublished == 1L)
        assert(stats.successCount == 1L)
        assert(stats.failureCount == 0L)
        assert(stats.averagePublishTime > 0.0)
        assert(stats.lastPublishTime > 0L)
    }
} 
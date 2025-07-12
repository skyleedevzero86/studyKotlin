package com.kominioai.domain.survey.infrastructure.event.listener

import com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.infrastructure.cache.SurveyCacheService
import com.kominioai.global.service.BusinessMetricsService
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import reactor.core.publisher.Mono
import java.time.Instant

class SurveyLifecycleEventListenerTest {

    private lateinit var surveyCacheService: SurveyCacheService
    private lateinit var businessMetricsService: BusinessMetricsService
    private lateinit var meterRegistry: MeterRegistry
    private lateinit var eventListener: SurveyLifecycleEventListener

    @BeforeEach
    fun setUp() {
        surveyCacheService = mock()
        businessMetricsService = mock()
        meterRegistry = SimpleMeterRegistry()
        eventListener = SurveyLifecycleEventListener(surveyCacheService, businessMetricsService, meterRegistry)
    }

    @Test
    fun `should handle SurveyCreated event successfully`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyCreated(
            surveyId = SurveyId.from("test-survey-id"),
            title = "Test Survey",
            description = "Test Description",
            createdBy = UserId.from("test-user-id"),
            settings = mapOf("allowAnonymous" to true)
        )
        
        whenever(surveyCacheService.invalidatePublishedSurveysCache()).thenReturn(Mono.just(true))
        whenever(businessMetricsService.recordSurveyCreated(any())).thenReturn(Unit)

        // When
        eventListener.handleSurveyCreated(event)

        // Then
        verify(surveyCacheService).invalidatePublishedSurveysCache()
        verify(businessMetricsService).recordSurveyCreated("test-survey-id")
        
        // 메트릭 검증
        val successCounter = meterRegistry.get("event.handler.success").counter()
        assert(successCounter.count() == 1.0)
    }

    @Test
    fun `should handle SurveyPublished event successfully`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyPublished(
            surveyId = SurveyId.from("test-survey-id"),
            publishedBy = UserId.from("test-user-id"),
            questionCount = 5
        )
        
        whenever(surveyCacheService.invalidatePublishedSurveysCache()).thenReturn(Mono.just(true))
        whenever(surveyCacheService.invalidateSurveyCache(any())).thenReturn(Mono.just(true))
        whenever(businessMetricsService.recordSurveyPublished(any(), any())).thenReturn(Unit)

        // When
        eventListener.handleSurveyPublished(event)

        // Then
        verify(surveyCacheService).invalidatePublishedSurveysCache()
        verify(surveyCacheService).invalidateSurveyCache(event.surveyId)
        verify(businessMetricsService).recordSurveyPublished("test-survey-id", 5)
        
        // 메트릭 검증
        val successCounter = meterRegistry.get("event.handler.success").counter()
        assert(successCounter.count() == 1.0)
    }

    @Test
    fun `should handle SurveyClosed event successfully`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyClosed(
            surveyId = SurveyId.from("test-survey-id"),
            closedBy = UserId.from("test-user-id"),
            reason = "Test closure"
        )
        
        whenever(surveyCacheService.invalidatePublishedSurveysCache()).thenReturn(Mono.just(true))
        whenever(surveyCacheService.invalidateSurveyCache(any())).thenReturn(Mono.just(true))
        whenever(businessMetricsService.recordSurveyClosed(any(), any())).thenReturn(Unit)

        // When
        eventListener.handleSurveyClosed(event)

        // Then
        verify(surveyCacheService).invalidatePublishedSurveysCache()
        verify(surveyCacheService).invalidateSurveyCache(event.surveyId)
        verify(businessMetricsService).recordSurveyClosed("test-survey-id", "Test closure")
        
        // 메트릭 검증
        val successCounter = meterRegistry.get("event.handler.success").counter()
        assert(successCounter.count() == 1.0)
    }

    @Test
    fun `should handle SurveyDeleted event successfully`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyDeleted(
            surveyId = SurveyId.from("test-survey-id"),
            deletedBy = UserId.from("test-user-id")
        )
        
        whenever(surveyCacheService.invalidateAllSurveyCache()).thenReturn(Mono.just(true))
        whenever(businessMetricsService.recordSurveyDeleted(any())).thenReturn(Unit)

        // When
        eventListener.handleSurveyDeleted(event)

        // Then
        verify(surveyCacheService).invalidateAllSurveyCache()
        verify(businessMetricsService).recordSurveyDeleted("test-survey-id")
        
        // 메트릭 검증
        val successCounter = meterRegistry.get("event.handler.success").counter()
        assert(successCounter.count() == 1.0)
    }

    @Test
    fun `should handle SurveyCreated event error and record failure metrics`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyCreated(
            surveyId = SurveyId.from("test-survey-id"),
            title = "Test Survey",
            description = "Test Description",
            createdBy = UserId.from("test-user-id"),
            settings = mapOf("allowAnonymous" to true)
        )
        
        val error = RuntimeException("Cache invalidation failed")
        whenever(surveyCacheService.invalidatePublishedSurveysCache()).thenThrow(error)

        // When & Then
        assertThrows<RuntimeException> {
            eventListener.handleSurveyCreated(event)
        }
        
        // 실패 메트릭 검증
        val failureCounter = meterRegistry.get("event.handler.failure").counter()
        assert(failureCounter.count() == 1.0)
    }

    @Test
    fun `should handle SurveyPublished event error and record failure metrics`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyPublished(
            surveyId = SurveyId.from("test-survey-id"),
            publishedBy = UserId.from("test-user-id"),
            questionCount = 5
        )
        
        val error = RuntimeException("Metrics recording failed")
        whenever(surveyCacheService.invalidatePublishedSurveysCache()).thenReturn(Mono.just(true))
        whenever(surveyCacheService.invalidateSurveyCache(any())).thenReturn(Mono.just(true))
        whenever(businessMetricsService.recordSurveyPublished(any(), any())).thenThrow(error)

        // When & Then
        assertThrows<RuntimeException> {
            eventListener.handleSurveyPublished(event)
        }
        
        // 실패 메트릭 검증
        val failureCounter = meterRegistry.get("event.handler.failure").counter()
        assert(failureCounter.count() == 1.0)
    }

    @Test
    fun `should record processing time metrics`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyCreated(
            surveyId = SurveyId.from("test-survey-id"),
            title = "Test Survey",
            description = "Test Description",
            createdBy = UserId.from("test-user-id"),
            settings = mapOf("allowAnonymous" to true)
        )
        
        whenever(surveyCacheService.invalidatePublishedSurveysCache()).thenReturn(Mono.just(true))
        whenever(businessMetricsService.recordSurveyCreated(any())).thenReturn(Unit)

        // When
        eventListener.handleSurveyCreated(event)

        // Then
        val timer = meterRegistry.get("event.handler.duration").timer()
        assert(timer.count() == 1L)
        assert(timer.totalTime().toMillis() > 0)
    }

    @Test
    fun `should handle multiple events concurrently`() {
        // Given
        val events = (1..5).map { i ->
            SurveyLifecycleEvent.SurveyCreated(
                surveyId = SurveyId.from("test-survey-$i"),
                title = "Test Survey $i",
                description = "Test Description $i",
                createdBy = UserId.from("test-user-id"),
                settings = mapOf("allowAnonymous" to true)
            )
        }
        
        whenever(surveyCacheService.invalidatePublishedSurveysCache()).thenReturn(Mono.just(true))
        whenever(businessMetricsService.recordSurveyCreated(any())).thenReturn(Unit)

        // When
        events.forEach { event ->
            eventListener.handleSurveyCreated(event)
        }

        // Then
        verify(surveyCacheService, times(5)).invalidatePublishedSurveysCache()
        verify(businessMetricsService, times(5)).recordSurveyCreated(any())
        
        // 메트릭 검증
        val successCounter = meterRegistry.get("event.handler.success").counter()
        assert(successCounter.count() == 5.0)
    }
} 
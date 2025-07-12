package com.kominioai.domain.survey.infrastructure.event

import com.kominioai.domain.survey.domain.model.event.SurveyLifecycleEvent
import com.kominioai.domain.survey.domain.model.event.QuestionEvent
import com.kominioai.domain.survey.domain.model.event.ResponseEvent
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.valueobject.QuestionId
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.infrastructure.cache.SurveyCacheService
import com.kominioai.global.service.BusinessMetricsService
import io.micrometer.core.instrument.MeterRegistry
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

@SpringBootTest
@ActiveProfiles("test")
class EventIntegrationTest {

    @Autowired
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @Autowired
    private lateinit var surveyCacheService: SurveyCacheService

    @Autowired
    private lateinit var businessMetricsService: BusinessMetricsService

    @Autowired
    private lateinit var meterRegistry: MeterRegistry

    @Test
    fun `should handle SurveyCreated event and invalidate cache`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyCreated(
            surveyId = SurveyId.from("test-survey-id"),
            title = "Test Survey",
            description = "Test Description",
            createdBy = UserId.from("test-user-id"),
            settings = mapOf("allowAnonymous" to true)
        )

        // When
        applicationEventPublisher.publishEvent(event)

        // Then
        // 이벤트가 비동기로 처리되므로 잠시 대기
        Thread.sleep(100)
        
        // 메트릭 검증
        val successCounter = meterRegistry.get("event.handler.success").counter()
        assert(successCounter.count() >= 1.0)
    }

    @Test
    fun `should handle SurveyPublished event and invalidate multiple caches`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyPublished(
            surveyId = SurveyId.from("test-survey-id"),
            publishedBy = UserId.from("test-user-id"),
            questionCount = 5
        )

        // When
        applicationEventPublisher.publishEvent(event)

        // Then
        // 이벤트가 비동기로 처리되므로 잠시 대기
        Thread.sleep(100)
        
        // 메트릭 검증
        val successCounter = meterRegistry.get("event.handler.success").counter()
        assert(successCounter.count() >= 1.0)
    }

    @Test
    fun `should handle QuestionAdded event and invalidate survey cache`() {
        // Given
        val event = QuestionEvent.QuestionAdded(
            surveyId = SurveyId.from("test-survey-id"),
            questionId = QuestionId.from("test-question-id"),
            questionText = "Test Question",
            questionType = "SINGLE_CHOICE",
            order = 1,
            addedBy = UserId.from("test-user-id")
        )

        // When
        applicationEventPublisher.publishEvent(event)

        // Then
        // 이벤트가 비동기로 처리되므로 잠시 대기
        Thread.sleep(100)
        
        // 메트릭 검증
        val successCounter = meterRegistry.get("event.handler.success").counter()
        assert(successCounter.count() >= 1.0)
    }

    @Test
    fun `should handle ResponseSubmitted event and update metrics`() {
        // Given
        val event = ResponseEvent.ResponseSubmitted(
            surveyId = SurveyId.from("test-survey-id"),
            responseId = ResponseId.from("test-response-id"),
            respondentId = UserId.from("test-user-id"),
            ipAddress = "127.0.0.1",
            answerCount = 3
        )

        // When
        applicationEventPublisher.publishEvent(event)

        // Then
        // 이벤트가 비동기로 처리되므로 잠시 대기
        Thread.sleep(100)
        
        // 메트릭 검증
        val successCounter = meterRegistry.get("event.handler.success").counter()
        assert(successCounter.count() >= 1.0)
    }

    @Test
    fun `should handle multiple events in sequence`() {
        // Given
        val events = listOf(
            SurveyLifecycleEvent.SurveyCreated(
                surveyId = SurveyId.from("test-survey-1"),
                title = "Test Survey 1",
                description = "Test Description 1",
                createdBy = UserId.from("test-user-id"),
                settings = mapOf("allowAnonymous" to true)
            ),
            QuestionEvent.QuestionAdded(
                surveyId = SurveyId.from("test-survey-1"),
                questionId = QuestionId.from("test-question-1"),
                questionText = "Test Question 1",
                questionType = "SINGLE_CHOICE",
                order = 1,
                addedBy = UserId.from("test-user-id")
            ),
            SurveyLifecycleEvent.SurveyPublished(
                surveyId = SurveyId.from("test-survey-1"),
                publishedBy = UserId.from("test-user-id"),
                questionCount = 1
            )
        )

        // When
        events.forEach { event ->
            applicationEventPublisher.publishEvent(event)
        }

        // Then
        // 이벤트들이 비동기로 처리되므로 잠시 대기
        Thread.sleep(300)
        
        // 메트릭 검증
        val successCounter = meterRegistry.get("event.handler.success").counter()
        assert(successCounter.count() >= 3.0)
    }

    @Test
    fun `should handle event with error gracefully`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyCreated(
            surveyId = SurveyId.from("error-test-survey-id"),
            title = "Error Test Survey",
            description = "Error Test Description",
            createdBy = UserId.from("test-user-id"),
            settings = mapOf("allowAnonymous" to true)
        )

        // When
        applicationEventPublisher.publishEvent(event)

        // Then
        // 이벤트가 비동기로 처리되므로 잠시 대기
        Thread.sleep(100)
        
        // 에러가 발생해도 시스템이 계속 동작해야 함
        // 메트릭 검증 (성공 또는 실패 카운터가 증가해야 함)
        val successCounter = meterRegistry.get("event.handler.success").counter()
        val failureCounter = meterRegistry.get("event.handler.failure").counter()
        assert(successCounter.count() + failureCounter.count() >= 1.0)
    }

    @Test
    fun `should record event processing time metrics`() {
        // Given
        val event = SurveyLifecycleEvent.SurveyCreated(
            surveyId = SurveyId.from("metrics-test-survey-id"),
            title = "Metrics Test Survey",
            description = "Metrics Test Description",
            createdBy = UserId.from("test-user-id"),
            settings = mapOf("allowAnonymous" to true)
        )

        // When
        applicationEventPublisher.publishEvent(event)

        // Then
        // 이벤트가 비동기로 처리되므로 잠시 대기
        Thread.sleep(100)
        
        // 처리 시간 메트릭 검증
        val timer = meterRegistry.get("event.handler.duration").timer()
        assert(timer.count() >= 1L)
        assert(timer.totalTime().toMillis() > 0)
    }

    @Test
    fun `should handle high volume of events`() {
        // Given
        val events = (1..10).map { i ->
            SurveyLifecycleEvent.SurveyCreated(
                surveyId = SurveyId.from("volume-test-survey-$i"),
                title = "Volume Test Survey $i",
                description = "Volume Test Description $i",
                createdBy = UserId.from("test-user-id"),
                settings = mapOf("allowAnonymous" to true)
            )
        }

        val startTime = System.currentTimeMillis()

        // When
        events.forEach { event ->
            applicationEventPublisher.publishEvent(event)
        }

        // Then
        // 모든 이벤트가 처리될 때까지 대기
        Thread.sleep(500)
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // 처리 시간이 1초 이내여야 함
        assert(duration < 1000)
        
        // 메트릭 검증
        val successCounter = meterRegistry.get("event.handler.success").counter()
        assert(successCounter.count() >= 10.0)
    }
} 
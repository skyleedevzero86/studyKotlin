package com.kominioai.global.util

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * MetricsUtils 테스트
 * 
 * @author KominioAI Team
 * @since 1.0.0
 */
class MetricsUtilsTest {

    private lateinit var meterRegistry: MeterRegistry
    private lateinit var metricsUtils: MetricsUtils

    @BeforeEach
    fun setUp() {
        meterRegistry = SimpleMeterRegistry()
        metricsUtils = MetricsUtils(meterRegistry)
    }

    @Test
    fun `API 응답 시간 메트릭 기록 테스트`() {
        // Given
        val endpoint = "/api/surveys"
        val method = "GET"
        val statusCode = 200
        val durationMs = 150L

        // When
        metricsUtils.recordApiResponseTime(endpoint, method, statusCode, durationMs)

        // Then
        val responseTimeSummary = meterRegistry.find("api.response.time").summary()
        val requestCounter = meterRegistry.find("api.requests.total").counter()
        val errorCounter = meterRegistry.find("api.errors.total").counter()

        assertAll(
            { assertNotNull(responseTimeSummary) },
            { assertEquals(1, responseTimeSummary.count()) },
            { assertEquals(150.0, responseTimeSummary.totalAmount()) },
            { assertNotNull(requestCounter) },
            { assertEquals(1.0, requestCounter.count()) },
            { assertNotNull(errorCounter) },
            { assertEquals(0.0, errorCounter.count()) }
        )
    }

    @Test
    fun `API 에러 메트릭 기록 테스트`() {
        // Given
        val endpoint = "/api/surveys"
        val method = "POST"
        val statusCode = 500
        val durationMs = 200L

        // When
        metricsUtils.recordApiResponseTime(endpoint, method, statusCode, durationMs)

        // Then
        val responseTimeSummary = meterRegistry.find("api.response.time").summary()
        val requestCounter = meterRegistry.find("api.requests.total").counter()
        val errorCounter = meterRegistry.find("api.errors.total").counter()

        assertAll(
            { assertNotNull(responseTimeSummary) },
            { assertEquals(1, responseTimeSummary.count()) },
            { assertEquals(200.0, responseTimeSummary.totalAmount()) },
            { assertNotNull(requestCounter) },
            { assertEquals(1.0, requestCounter.count()) },
            { assertNotNull(errorCounter) },
            { assertEquals(1.0, errorCounter.count()) }
        )
    }

    @Test
    fun `비즈니스 메트릭 기록 테스트`() {
        // Given
        val metricName = "survey.created"
        val value = 1.0
        val tags = mapOf("surveyId" to "test-123", "userId" to "user-456")

        // When
        metricsUtils.recordBusinessMetric(metricName, value, tags)

        // Then
        val businessMetric = meterRegistry.find("business.survey.created").summary()
        assertAll(
            { assertNotNull(businessMetric) },
            { assertEquals(1, businessMetric.count()) },
            { assertEquals(1.0, businessMetric.totalAmount()) }
        )
    }

    @Test
    fun `시스템 메트릭 기록 테스트`() {
        // Given
        val metricName = "active.users"
        val value = 100.0
        val tags = mapOf("environment" to "test")

        // When
        metricsUtils.recordSystemMetric(metricName, value, tags)

        // Then
        val systemMetric = meterRegistry.find("system.active.users").gauge()
        assertAll(
            { assertNotNull(systemMetric) },
            { assertEquals(100.0, systemMetric.value()) }
        )
    }

    @Test
    fun `카운터 증가 테스트`() {
        // Given
        val counterName = "test.counter"
        val tags = mapOf("tag1" to "value1")

        // When
        metricsUtils.incrementCounter(counterName, tags)
        metricsUtils.incrementCounter(counterName, tags, 2.0)

        // Then
        val counter = meterRegistry.find("test.counter").counter()
        assertAll(
            { assertNotNull(counter) },
            { assertEquals(3.0, counter.count()) }
        )
    }

    @Test
    fun `분포 요약 기록 테스트`() {
        // Given
        val summaryName = "test.summary"
        val value = 50.0
        val tags = mapOf("tag1" to "value1")

        // When
        metricsUtils.recordDistributionSummary(summaryName, value, tags)

        // Then
        val summary = meterRegistry.find("test.summary").summary()
        assertAll(
            { assertNotNull(summary) },
            { assertEquals(1, summary.count()) },
            { assertEquals(50.0, summary.totalAmount()) }
        )
    }

    @Test
    fun `타이머 사용 테스트`() {
        // Given
        val timerName = "test.timer"
        val tags = mapOf("operation" to "test")

        // When
        val sample = metricsUtils.startTimer(timerName, tags)
        Thread.sleep(10) // 10ms 대기
        metricsUtils.stopTimer(sample, timerName, tags)

        // Then
        val timer = meterRegistry.find("test.timer").timer()
        assertAll(
            { assertNotNull(timer) },
            { assertEquals(1, timer.count()) },
            { assert(timer.totalTime() > 0) }
        )
    }
} 
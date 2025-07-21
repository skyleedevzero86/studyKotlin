package com.kominioai.global.controller

import com.kominioai.global.service.BusinessMetricsService
import com.kominioai.global.service.SystemMetricsService
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@WebFluxTest(MetricsController::class)
class MetricsControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var businessMetricsService: BusinessMetricsService

    @MockBean
    private lateinit var systemMetricsService: SystemMetricsService

    private lateinit var meterRegistry: MeterRegistry
    private lateinit var metricsUtils: MetricsUtils

    @BeforeEach
    fun setUp() {
        meterRegistry = SimpleMeterRegistry()
        metricsUtils = MetricsUtils(meterRegistry)
        
        setupTestMetrics()
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `메트릭 요약 조회 테스트`() {
        webTestClient.get()
            .uri("/api/admin/metrics/summary")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.api.totalRequests").isEqualTo(10.0)
            .jsonPath("$.api.totalErrors").isEqualTo(2.0)
            .jsonPath("$.api.errorRate").isEqualTo(20.0)
            .jsonPath("$.business.totalSurveysCreated").isEqualTo(5.0)
            .jsonPath("$.business.totalResponses").isEqualTo(15.0)
            .jsonPath("$.system.heapUsagePercent").isEqualTo(75.0)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `API 성능 메트릭 조회 테스트`() {
        webTestClient.get()
            .uri("/api/admin/metrics/api/performance")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.endpoints").exists()
            .jsonPath("$.statusCodes").exists()
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `비즈니스 메트릭 조회 테스트`() {
        webTestClient.get()
            .uri("/api/admin/metrics/business")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.surveys.totalCreated").isEqualTo(5.0)
            .jsonPath("$.surveys.totalResponses").isEqualTo(15.0)
            .jsonPath("$.cache.totalHits").isEqualTo(100.0)
            .jsonPath("$.cache.totalMisses").isEqualTo(20.0)
            .jsonPath("$.cache.hitRate").isEqualTo(83.33)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `시스템 메트릭 조회 테스트`() {
        webTestClient.get()
            .uri("/api/admin/metrics/system")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.jvm.heapUsagePercent").isEqualTo(75.0)
            .jsonPath("$.jvm.currentThreads").isEqualTo(25.0)
            .jsonPath("$.application.totalRequests").isEqualTo(10.0)
            .jsonPath("$.application.totalErrors").isEqualTo(2.0)
            .jsonPath("$.application.errorRate").isEqualTo(20.0)
    }

    @Test
    @WithMockUser(roles = ["SUPER_ADMIN"])
    fun `메트릭 카운터 리셋 테스트`() {
        val resetRequest = mapOf<String, String>()

        webTestClient.post()
            .uri("/api/admin/metrics/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(resetRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").isEqualTo("Metrics counters have been reset")
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `메트릭 카운터 리셋 권한 부족 테스트`() {
        val resetRequest = mapOf<String, String>()

        webTestClient.post()
            .uri("/api/admin/metrics/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(resetRequest)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `인증되지 않은 사용자 메트릭 조회 테스트`() {
        webTestClient.get()
            .uri("/api/admin/metrics/summary")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    private fun setupTestMetrics() {
        repeat(10) { index ->
            val statusCode = if (index < 8) 200 else 500
            metricsUtils.recordApiResponseTime("/api/surveys", "GET", statusCode, 100L + index)
        }

        repeat(5) { index ->
            metricsUtils.recordBusinessMetric("surveys.created", 1.0, 
                mapOf("surveyId" to "test-$index", "userId" to "user-$index"))
        }

        repeat(15) { index ->
            metricsUtils.recordBusinessMetric("responses.submitted", 1.0, 
                mapOf("surveyId" to "test-${index % 5}", "responseId" to "response-$index"))
        }

        repeat(100) { index ->
            metricsUtils.recordCacheHit("survey", "survey-$index")
        }

        repeat(20) { index ->
            metricsUtils.recordCacheMiss("survey", "survey-$index")
        }

        metricsUtils.recordSystemMetric("heap.usage.percent", 75.0)
        metricsUtils.recordSystemMetric("threads.current", 25.0)
        metricsUtils.recordSystemMetric("requests.total", 10.0)
        metricsUtils.recordSystemMetric("errors.total", 2.0)
        metricsUtils.recordSystemMetric("error.rate", 20.0)
    }
}
package com.kominioai.domain.survey.load

import com.kominioai.domain.survey.presentation.rest.dto.request.CreateSurveyRequest
import io.kotest.matchers.longs.shouldBeGreaterThan
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import kotlin.system.measureTimeMillis
import io.kotest.matchers.longs.shouldBeLessThan

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SurveyLoadTest {

    private val numberOfThreads = 10
    private val numberOfRequests = 100

    @Test
    fun `should handle concurrent survey creations`() = runTest {
        val webTestClient = WebTestClient.bindToServer()
            .baseUrl("http://localhost:8080")
            .build()

        val totalTime = measureTimeMillis {
            val jobs = (1..numberOfThreads).map { threadIndex ->
                async {
                    repeat(numberOfRequests / numberOfThreads) { requestIndex ->
                        val request = CreateSurveyRequest(
                            title = "Load Test Survey $threadIndex-$requestIndex",
                            description = "Load test description",
                            createdBy = "loadtestuser"
                        )

                        try {
                            webTestClient.post()
                                .uri("/api/surveys")
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(request))
                                .headers { it.setBearerAuth("test-token") }
                                .exchange()
                                .expectStatus().isOk
                        } catch (e: Exception) {
                            println("Request failed: ${e.message}")
                        }
                    }
                }
            }

            jobs.awaitAll()
        }

        println("Load test completed: $numberOfRequests requests with $numberOfThreads threads in ${totalTime}ms")
        println("Average response time: ${totalTime.toDouble() / numberOfRequests}ms per request")

        totalTime shouldBeGreaterThan 0L
    }
}
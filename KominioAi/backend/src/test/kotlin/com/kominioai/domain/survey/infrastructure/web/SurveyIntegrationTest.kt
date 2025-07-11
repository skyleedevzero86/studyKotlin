package com.kominioai.domain.survey.infrastructure.web

import com.kominioai.domain.survey.application.port.input.command.AnswerSubmission
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.domain.Question
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.model.SurveySettings
import com.kominioai.domain.survey.presentation.rest.dto.request.CreateSurveyRequest
import com.kominioai.domain.survey.presentation.rest.dto.request.SubmitResponseRequest
import com.kominioai.domain.survey.presentation.rest.dto.common.SurveyDto
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyResponseDto
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.springframework.web.reactive.function.BodyInserters
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = ["classpath:application-test.yml"])
@Testcontainers
class SurveyIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:15").apply {
            withDatabaseName("survey_test")
            withUsername("test")
            withPassword("test")
        }

        @Container
        @JvmStatic
        val redis = GenericContainer<Nothing>("redis:7-alpine").apply {
            withExposedPorts(6379)
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.redis.host", redis::getHost)
            registry.add("spring.redis.port", redis::getFirstMappedPort)
        }
    }

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var surveyRepository: SurveyRepository

    @Autowired
    private lateinit var surveyResponseRepository: SurveyResponseRepository

    @Test
    fun `should create survey via REST API`() = runTest {
        // Given
        val request = CreateSurveyRequest(
            title = "Integration Test Survey",
            description = "Test Description",
            createdBy = "testuser",
            allowAnonymous = true,
            allowMultipleResponses = false,
            requireLogin = false,
            collectIpAddress = false
        )

        // When & Then
        webTestClient.post()
            .uri("/api/surveys")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .headers { it.setBearerAuth("test-token") }
            .exchange()
            .expectStatus().isCreated
            .expectBody()
    }

    @Test
    fun `should publish survey and submit response`() = runTest {
        // Given
        val userId = UserId.from("testuser")
        val survey = Survey.create(
            title = "Test Survey",
            description = "Test Description",
            createdBy = userId,
            settings = SurveySettings()
        )

        val question = Question.create(
            surveyId = survey.id,
            order = 1,
            text = "What is your name?",
            description = null,
            type = QuestionType.TEXT,
            required = true,
            options = emptyList()
        )
        val surveyWithQuestion = survey.addQuestion(question)

        val savedSurvey = surveyRepository.save(surveyWithQuestion).block()

        // When
        webTestClient.post()
            .uri("/api/surveys/${savedSurvey?.id?.value}/publish")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(mapOf("userId" to "testuser")))
            .headers { it.setBearerAuth("test-token") }
            .exchange()
            .expectStatus().isOk

        val responseRequest = SubmitResponseRequest(
            surveyId = savedSurvey?.id?.value ?: "",
            answers = listOf(
                AnswerSubmission(
                    questionId = question.id.value,
                    answerText = "John Doe"
                )
            )
        )

        webTestClient.post()
            .uri("/api/responses")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(responseRequest))
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should return 404 when survey not found`() {
        webTestClient.get()
            .uri("/api/surveys/nonexistent")
            .headers { it.setBearerAuth("test-token") }
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should validate required fields in survey creation`() {
        val invalidRequest = CreateSurveyRequest(
            title = "",
            description = "Test Description",
            createdBy = "testuser"
        )

        webTestClient.post()
            .uri("/api/surveys")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(invalidRequest))
            .headers { it.setBearerAuth("test-token") }
            .exchange()
            .expectStatus().isBadRequest
    }
}
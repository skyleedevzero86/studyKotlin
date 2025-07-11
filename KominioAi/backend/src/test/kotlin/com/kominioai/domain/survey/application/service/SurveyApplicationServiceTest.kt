package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.command.CreateSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.PublishSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.SubmitResponseCommand
import com.kominioai.domain.survey.application.port.output.EventPublisher
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.domain.Question
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.model.domain.SurveyResponse
import com.kominioai.domain.survey.domain.model.service.SurveyDomainService
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.model.SurveySettings
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Mono

@ExtendWith(MockKExtension::class)
class SurveyApplicationServiceTest {

    private val surveyRepository: SurveyRepository = mockk()
    private val surveyResponseRepository: SurveyResponseRepository = mockk()
    private val eventPublisher: EventPublisher = mockk()
    private val surveyDomainService: SurveyDomainService = mockk()

    @InjectMockKs
    private lateinit var surveyApplicationService: SurveyApplicationService

    @Test
    fun `should create survey successfully`() = runTest {
        // Given
        val command = CreateSurveyCommand(
            title = "Test Survey",
            description = "Test Description",
            createdBy = UserId.from("user123"),
            settings = SurveySettings()
        )

        val expectedSurvey = Survey.create(
            title = command.title,
            description = command.description,
            createdBy = command.createdBy,
            settings = command.settings
        )

        coEvery { surveyDomainService.createSurvey(any(), any(), any()) } returns Mono.just(expectedSurvey)

        // When
        val result = surveyApplicationService.createSurvey(command)

        // Then
        result shouldNotBe null
        coVerify { surveyDomainService.createSurvey(command.title, command.description, command.createdBy) }
    }

    @Test
    fun `should throw exception when survey not found for publishing`() = runTest {
        // Given
        val command = PublishSurveyCommand(SurveyId.from("nonexistent"), UserId.from("user123"))
        coEvery { surveyRepository.findById(any()) } returns Mono.empty()

        // When & Then
        shouldThrow<Exception> {
            surveyApplicationService.publishSurvey(command)
        }
    }

    @Test
    fun `should publish survey successfully`() = runTest {
        // Given
        val surveyId = SurveyId.from("survey123")
        val userId = UserId.from("user123")
        val command = PublishSurveyCommand(surveyId, userId)

        val draftSurvey = Survey.create(
            title = "Test Survey",
            description = "Test Description",
            createdBy = userId,
            settings = SurveySettings()
        )

        val question = Question.create(
            surveyId = surveyId,
            order = 1,
            text = "Test Question",
            description = null,
            type = QuestionType.TEXT,
            required = true,
            options = emptyList()
        )

        val surveyWithQuestion = draftSurvey.addQuestion(question)
        val publishedSurvey = surveyWithQuestion.publish()

        coEvery { surveyRepository.findById(surveyId) } returns Mono.just(surveyWithQuestion)
        coEvery { surveyRepository.save(any()) } returns Mono.just(publishedSurvey)

        // When
        val result = surveyApplicationService.publishSurvey(command)

        // Then
        result shouldNotBe null
        coVerify { surveyRepository.save(any()) }
    }

    @Test
    fun `should validate survey response and submit successfully`() = runTest {
        // Given
        val surveyId = SurveyId.from("survey123")
        val userId = UserId.from("user123")
        val survey = Survey.create(
            title = "Test Survey",
            description = "Test Description",
            createdBy = userId,
            settings = SurveySettings()
        )

        val question = Question.create(
            surveyId = surveyId,
            order = 1,
            text = "Test Question",
            description = null,
            type = QuestionType.TEXT,
            required = true,
            options = emptyList()
        )
        val surveyWithQuestion = survey.addQuestion(question)

        val answer = com.kominioai.domain.survey.domain.model.domain.Answer.create(
            responseId = "",
            questionId = question.id,
            questionType = question.type,
            textAnswer = "Test Answer",
            selectedOptions = emptyList()
        )

        val command = SubmitResponseCommand(
            surveyId = surveyId,
            respondentId = "respondent123",
            answers = listOf(answer),
            ipAddress = "127.0.0.1"
        )

        val expectedResponse = SurveyResponse.create(
            surveyId = surveyId,
            respondentId = UserId.from("respondent123"),
            answers = listOf(answer),
            ipAddress = "127.0.0.1"
        )

        coEvery { surveyRepository.findById(surveyId) } returns Mono.just(surveyWithQuestion)
        coEvery { surveyResponseRepository.save(any()) } returns Mono.just(expectedResponse)

        // When
        val result = surveyApplicationService.submitResponse(command)

        // Then
        result shouldNotBe null
        coVerify { surveyResponseRepository.save(any()) }
    }
}
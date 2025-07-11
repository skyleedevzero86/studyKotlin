package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.command.AnswerSubmission
import com.kominioai.domain.survey.application.port.input.command.CreateSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.PublishSurveyCommand
import com.kominioai.domain.survey.application.port.input.command.SubmitResponseCommand
import com.kominioai.domain.survey.application.port.output.EventPublisher
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.Question
import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.Survey
import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.SurveyResponse
import com.kominioai.domain.survey.domain.model.event.SurveyEvent
import com.kominioai.domain.survey.domain.model.service.SurveyDomainService
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

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
            createdBy = UserId("user123")
        )

        val expectedSurvey = Survey(
            id = SurveyId("survey123"),
            title = command.title,
            description = command.description,
            status = SurveyStatus.DRAFT,
            createdBy = command.createdBy,
            createdAt = Instant.now()
        )

        coEvery { surveyRepository.save(any()) } returns expectedSurvey
        coEvery { eventPublisher.publish(any()) } just Runs

        // When
        val result = surveyApplicationService.createSurvey(command)

        // Then
        result.title shouldBe command.title
        result.description shouldBe command.description
        result.status shouldBe SurveyStatus.DRAFT
        result.createdBy shouldBe command.createdBy.value

        coVerify { surveyRepository.save(any()) }
        coVerify { eventPublisher.publish(any<SurveyEvent.SurveyCreated>()) }
    }

    @Test
    fun `should throw exception when survey not found for publishing`() = runTest {
        // Given
        val command = PublishSurveyCommand(SurveyId("nonexistent"))
        coEvery { surveyRepository.findById(any()) } returns null

        // When & Then
        shouldThrow<IllegalArgumentException> {
            surveyApplicationService.publishSurvey(command)
        }.message shouldBe "Survey not found"
    }

    @Test
    fun `should publish survey successfully`() = runTest {
        // Given
        val surveyId = SurveyId("survey123")
        val command = PublishSurveyCommand(surveyId)

        val draftSurvey = Survey(
            id = surveyId,
            title = "Test Survey",
            description = "Test Description",
            status = SurveyStatus.DRAFT,
            createdBy = UserId("user123"),
            createdAt = Instant.now()
        ).apply {
            addQuestion(
                Question(
                    surveyId = surveyId,
                    title = "Test Question",
                    type = QuestionType.TEXT,
                    isRequired = true,
                    orderIndex = 1
                )
            )
        }

        val publishedSurvey = draftSurvey.copy(
            status = SurveyStatus.PUBLISHED,
            publishedAt = Instant.now()
        )

        coEvery { surveyRepository.findById(surveyId) } returns draftSurvey
        coEvery { surveyRepository.save(any()) } returns publishedSurvey
        coEvery { eventPublisher.publish(any()) } just Runs

        // When
        val result = surveyApplicationService.publishSurvey(command)

        // Then
        result.status shouldBe SurveyStatus.PUBLISHED
        result.publishedAt shouldNotBe null

        coVerify { surveyRepository.save(any()) }
        coVerify { eventPublisher.publish(any<SurveyEvent.SurveyPublished>()) }
    }

    @Test
    fun `should validate survey response and submit successfully`() = runTest {
        // Given
        val surveyId = SurveyId("survey123")
        val survey = Survey(
            id = surveyId,
            title = "Test Survey",
            description = "Test Description",
            status = SurveyStatus.PUBLISHED,
            createdBy = UserId("user123"),
            createdAt = Instant.now()
        )

        val question = Question(
            id = "question123",
            surveyId = surveyId,
            title = "Test Question",
            type = QuestionType.TEXT,
            isRequired = true,
            orderIndex = 1
        )
        survey.addQuestion(question)

        val command = SubmitResponseCommand(
            surveyId = surveyId,
            respondentId = UserId("respondent123"),
            answers = listOf(
                AnswerSubmission(
                    questionId = question.id,
                    answerText = "Test Answer"
                )
            )
        )

        val expectedResponse = SurveyResponse(
            id = ResponseId("response123"),
            surveyId = surveyId,
            respondentId = UserId("respondent123"),
            submittedAt = Instant.now()
        )

        coEvery { surveyRepository.findById(surveyId) } returns survey
        coEvery { surveyDomainService.validateSurveyResponse(any(), any()) } returns emptyList()
        coEvery { surveyResponseRepository.save(any()) } returns expectedResponse
        coEvery { eventPublisher.publish(any()) } just Runs

        // When
        val result = surveyApplicationService.submitResponse(command)

        // Then
        result.surveyId shouldBe surveyId.value
        result.respondentId shouldBe "respondent123"
        result.answers.size shouldBe 1

        coVerify { surveyResponseRepository.save(any()) }
        coVerify { eventPublisher.publish(any<SurveyEvent.SurveyCompleted>()) }
    }
}
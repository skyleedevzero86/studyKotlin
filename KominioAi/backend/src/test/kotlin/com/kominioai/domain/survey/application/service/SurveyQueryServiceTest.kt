package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.query.GetSurveyQuery
import com.kominioai.domain.survey.application.port.input.query.GetUserSurveysQuery
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.Survey
import com.kominioai.domain.survey.domain.model.service.SurveyDomainService
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
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
class SurveyQueryServiceTest {

    private val surveyRepository: SurveyRepository = mockk()
    private val surveyResponseRepository: SurveyResponseRepository = mockk()
    private val surveyDomainService: SurveyDomainService = mockk()

    private val surveyQueryService = SurveyQueryService(
        surveyRepository = surveyRepository,
        surveyResponseRepository = surveyResponseRepository,
        surveyDomainService = surveyDomainService
    )

    @Test
    fun `should return survey when found`() = runTest {
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

        coEvery { surveyRepository.findById(surveyId) } returns survey

        // When
        val result = surveyQueryService.getSurvey(GetSurveyQuery(surveyId))

        // Then
        result shouldNotBe null
        result?.id shouldBe surveyId.value
        result?.title shouldBe survey.title
    }

    @Test
    fun `should return null when survey not found`() = runTest {
        // Given
        val surveyId = SurveyId("nonexistent")
        coEvery { surveyRepository.findById(surveyId) } returns null

        // When
        val result = surveyQueryService.getSurvey(GetSurveyQuery(surveyId))

        // Then
        result shouldBe null
    }

    @Test
    fun `should return user surveys`() = runTest {
        // Given
        val userId = UserId("user123")
        val surveys = listOf(
            Survey(
                id = SurveyId("survey1"),
                title = "Survey 1",
                description = "Description 1",
                status = SurveyStatus.DRAFT,
                createdBy = userId,
                createdAt = Instant.now()
            ),
            Survey(
                id = SurveyId("survey2"),
                title = "Survey 2",
                description = "Description 2",
                status = SurveyStatus.PUBLISHED,
                createdBy = userId,
                createdAt = Instant.now()
            )
        )

        coEvery { surveyRepository.findByCreatedBy(userId) } returns surveys

        // When
        val result = surveyQueryService.getUserSurveys(GetUserSurveysQuery(userId))

        // Then
        result.size shouldBe 2
        result[0].id shouldBe "survey1"
        result[1].id shouldBe "survey2"
    }
}
package com.kominioai.domain.survey.application.service

import com.kominioai.domain.survey.application.port.input.query.GetSurveyQuery
import com.kominioai.domain.survey.application.port.input.query.GetUserSurveysQuery
import com.kominioai.domain.survey.application.port.output.SurveyRepository
import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.model.service.SurveyDomainService
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.model.SurveySettings
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

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
        val surveyId = SurveyId.from("survey123")
        val userId = UserId.from("user123")
        val survey = Survey.create(
            title = "Test Survey",
            description = "Test Description",
            createdBy = userId,
            settings = SurveySettings()
        )

        coEvery { surveyRepository.findByIdWithQuestions(surveyId) } returns Mono.just(survey)

        // When
        val result = surveyQueryService.getSurvey(GetSurveyQuery(surveyId))

        // Then
        result shouldNotBe null
        coVerify { surveyRepository.findByIdWithQuestions(surveyId) }
    }

    @Test
    fun `should return empty when survey not found`() = runTest {
        // Given
        val surveyId = SurveyId.from("nonexistent")
        coEvery { surveyRepository.findByIdWithQuestions(surveyId) } returns Mono.empty()

        // When
        val result = surveyQueryService.getSurvey(GetSurveyQuery(surveyId))

        // Then
        result shouldBe null
    }

    @Test
    fun `should return user surveys`() = runTest {
        // Given
        val userId = UserId.from("user123")
        val surveys = listOf(
            Survey.create(
                title = "Survey 1",
                description = "Description 1",
                createdBy = userId,
                settings = SurveySettings()
            ),
            Survey.create(
                title = "Survey 2",
                description = "Description 2",
                createdBy = userId,
                settings = SurveySettings()
            )
        )

        coEvery { surveyRepository.findByCreatedBy(userId) } returns Flux.fromIterable(surveys)

        // When
        val result = surveyQueryService.getUserSurveys(GetUserSurveysQuery(userId))

        // Then
        result shouldNotBe null
        coVerify { surveyRepository.findByCreatedBy(userId) }
    }
}
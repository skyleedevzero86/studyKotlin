package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.adapter

import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.entity.Survey as SurveyEntity
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.SurveyR2dbcRepository
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.QuestionR2dbcRepository
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.QuestionOptionR2dbcRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

class R2dbcSurveyRepositoryAdapterPagingTest {

    private lateinit var adapter: R2dbcSurveyRepositoryAdapter
    private lateinit var surveyRepository: SurveyR2dbcRepository
    private lateinit var questionRepository: QuestionR2dbcRepository
    private lateinit var questionOptionRepository: QuestionOptionR2dbcRepository
    private lateinit var surveyDataLoader: SurveyDataLoader
    private lateinit var performanceMetrics: SurveyPerformanceMetrics

    @BeforeEach
    fun setUp() {
        surveyRepository = mockk()
        questionRepository = mockk()
        questionOptionRepository = mockk()
        surveyDataLoader = mockk()
        performanceMetrics = mockk(relaxed = true)
        
        adapter = R2dbcSurveyRepositoryAdapter(
            surveyRepository,
            questionRepository,
            questionOptionRepository,
            surveyDataLoader,
            performanceMetrics
        )
    }

    @Test
    fun `findAllWithPaging should return correct page with total count`() {
        // Given
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "created_at"))
        val surveys = createMockSurveys(10)
        val totalCount = 100L
        
        coEvery { surveyRepository.findAllWithPaging(10L, 0L) } returns Flux.fromIterable(surveys)
        coEvery { surveyRepository.countAll() } returns Mono.just(totalCount)
        coEvery { surveyDataLoader.loadSurveysWithQuestionsAndOptions(any()) } returns Mono.just(emptyMap())
        
        // When & Then
        StepVerifier.create(adapter.findAllWithPaging(pageable))
            .expectNextMatches { page ->
                page.content.size == 10 &&
                page.totalElements == totalCount &&
                page.number == 0 &&
                page.size == 10
            }
            .verifyComplete()
    }

    @Test
    fun `findAllWithPaging should handle empty result`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        
        coEvery { surveyRepository.findAllWithPaging(10L, 0L) } returns Flux.empty()
        coEvery { surveyRepository.countAll() } returns Mono.just(0L)
        
        // When & Then
        StepVerifier.create(adapter.findAllWithPaging(pageable))
            .expectNextMatches { page ->
                page.content.isEmpty() &&
                page.totalElements == 0L
            }
            .verifyComplete()
    }

    @Test
    fun `findAllWithPaging should handle second page`() {
        // Given
        val pageable = PageRequest.of(1, 10) // 두 번째 페이지
        val surveys = createMockSurveys(10)
        val totalCount = 100L
        
        coEvery { surveyRepository.findAllWithPaging(10L, 10L) } returns Flux.fromIterable(surveys)
        coEvery { surveyRepository.countAll() } returns Mono.just(totalCount)
        coEvery { surveyDataLoader.loadSurveysWithQuestionsAndOptions(any()) } returns Mono.just(emptyMap())
        
        // When & Then
        StepVerifier.create(adapter.findAllWithPaging(pageable))
            .expectNextMatches { page ->
                page.content.size == 10 &&
                page.totalElements == totalCount &&
                page.number == 1 &&
                page.size == 10
            }
            .verifyComplete()
    }

    @Test
    fun `findByStatusWithPaging should return filtered results`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val status = SurveyStatus.PUBLISHED
        val surveys = createMockSurveys(5).map { it.copy(status = status) }
        val totalCount = 25L
        
        coEvery { surveyRepository.findByStatusWithPaging(status, 10L, 0L) } returns Flux.fromIterable(surveys)
        coEvery { surveyRepository.countByStatus(status) } returns Mono.just(totalCount)
        coEvery { surveyDataLoader.loadSurveysWithQuestionsAndOptions(any()) } returns Mono.just(emptyMap())
        
        // When & Then
        StepVerifier.create(adapter.findByStatusWithPaging(status, pageable))
            .expectNextMatches { page ->
                page.content.size == 5 &&
                page.totalElements == totalCount &&
                page.content.all { it.status == status }
            }
            .verifyComplete()
    }

    @Test
    fun `findByCreatedByWithPaging should return user specific surveys`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val userId = UserId.from("user-123")
        val surveys = createMockSurveys(3).map { it.copy(createdBy = userId) }
        val totalCount = 15L
        
        coEvery { surveyRepository.findByCreatedByWithPaging(userId.value, 10L, 0L) } returns Flux.fromIterable(surveys)
        coEvery { surveyRepository.countByCreatedBy(userId.value) } returns Mono.just(totalCount)
        coEvery { surveyDataLoader.loadSurveysWithQuestionsAndOptions(any()) } returns Mono.just(emptyMap())
        
        // When & Then
        StepVerifier.create(adapter.findByCreatedByWithPaging(userId, pageable))
            .expectNextMatches { page ->
                page.content.size == 3 &&
                page.totalElements == totalCount &&
                page.content.all { it.createdBy == userId }
            }
            .verifyComplete()
    }

    @Test
    fun `findPublishedSurveysWithPaging should return only published surveys`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val surveys = createMockSurveys(8).map { it.copy(status = SurveyStatus.PUBLISHED) }
        val totalCount = 42L
        
        coEvery { surveyRepository.findPublishedSurveysWithPaging(10L, 0L) } returns Flux.fromIterable(surveys)
        coEvery { surveyRepository.countPublishedSurveys() } returns Mono.just(totalCount)
        coEvery { surveyDataLoader.loadSurveysWithQuestionsAndOptions(any()) } returns Mono.just(emptyMap())
        
        // When & Then
        StepVerifier.create(adapter.findPublishedSurveysWithPaging(pageable))
            .expectNextMatches { page ->
                page.content.size == 8 &&
                page.totalElements == totalCount &&
                page.content.all { it.status == SurveyStatus.PUBLISHED }
            }
            .verifyComplete()
    }

    @Test
    fun `count methods should return correct counts`() {
        // Given
        coEvery { surveyRepository.countAll() } returns Mono.just(100L)
        coEvery { surveyRepository.countByStatus(SurveyStatus.PUBLISHED) } returns Mono.just(50L)
        coEvery { surveyRepository.countByCreatedBy("user-123") } returns Mono.just(25L)
        coEvery { surveyRepository.countPublishedSurveys() } returns Mono.just(50L)
        
        // When & Then
        StepVerifier.create(adapter.countAll())
            .expectNext(100L)
            .verifyComplete()
            
        StepVerifier.create(adapter.countByStatus(SurveyStatus.PUBLISHED))
            .expectNext(50L)
            .verifyComplete()
            
        StepVerifier.create(adapter.countByCreatedBy(UserId.from("user-123")))
            .expectNext(25L)
            .verifyComplete()
            
        StepVerifier.create(adapter.countPublishedSurveys())
            .expectNext(50L)
            .verifyComplete()
    }

    @Test
    fun `findAllWithPaging should handle custom sorting`() {
        // Given
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "title"))
        val surveys = createMockSurveys(10)
        val totalCount = 100L
        
        coEvery { surveyRepository.findAllWithPagingAndSorting("title", "asc", 10L, 0L) } returns Flux.fromIterable(surveys)
        coEvery { surveyRepository.countAll() } returns Mono.just(totalCount)
        coEvery { surveyDataLoader.loadSurveysWithQuestionsAndOptions(any()) } returns Mono.just(emptyMap())
        
        // When & Then
        StepVerifier.create(adapter.findAllWithPaging(pageable))
            .expectNextMatches { page ->
                page.content.size == 10 &&
                page.totalElements == totalCount
            }
            .verifyComplete()
    }

    private fun createMockSurveys(count: Int): List<SurveyEntity> {
        return (1..count).map { index ->
            SurveyEntity(
                id = "survey-$index",
                title = "Survey $index",
                description = "Description $index",
                createdBy = "user-$index",
                createdAt = LocalDateTime.now().minusDays(index.toLong()),
                updatedAt = LocalDateTime.now().minusDays(index.toLong()),
                status = SurveyStatus.DRAFT,
                allowAnonymous = true,
                allowMultipleResponses = false,
                requireLogin = false,
                collectIpAddress = false
            )
        }
    }
} 
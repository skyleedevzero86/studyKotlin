package com.kominioai.domain.survey.infrastructure.persistence.r2dbc.adapter

import com.kominioai.domain.survey.domain.model.domain.Question
import com.kominioai.domain.survey.domain.model.domain.QuestionOption
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.valueobject.QuestionType
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import com.kominioai.domain.survey.domain.model.SurveySettings
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.QuestionR2dbcRepository
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.QuestionOptionR2dbcRepository
import com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository.SurveyR2dbcRepository
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class SurveyRepositoryPerformanceTest {

    @Autowired
    private lateinit var surveyRepository: SurveyR2dbcRepository

    @Autowired
    private lateinit var questionRepository: QuestionR2dbcRepository

    @Autowired
    private lateinit var questionOptionRepository: QuestionOptionR2dbcRepository

    private lateinit var surveyDataLoader: SurveyDataLoader
    private lateinit var performanceMetrics: SurveyPerformanceMetrics
    private lateinit var r2dbcSurveyRepositoryAdapter: R2dbcSurveyRepositoryAdapter

    @BeforeEach
    fun setUp() {
        val meterRegistry = SimpleMeterRegistry()
        performanceMetrics = SurveyPerformanceMetrics(meterRegistry)
        surveyDataLoader = SurveyDataLoader(questionRepository, questionOptionRepository)
        r2dbcSurveyRepositoryAdapter = R2dbcSurveyRepositoryAdapter(
            surveyRepository,
            questionRepository,
            questionOptionRepository,
            surveyDataLoader,
            performanceMetrics
        )
    }

    @Test
    fun `성능 최적화된 설문지 로딩 테스트`() {
        // Given: 복잡한 설문지 생성 (10개 질문, 각각 5개 옵션)
        val survey = createComplexSurvey(10, 5)
        val savedSurvey = saveSurveyWithQuestions(survey).block()!!

        // When: 성능 최적화된 메서드로 설문지 로딩
        val startTime = System.currentTimeMillis()
        val loadedSurvey = r2dbcSurveyRepositoryAdapter.findByIdWithQuestions(savedSurvey.id).block()
        val endTime = System.currentTimeMillis()

        // Then: 성능 검증
        StepVerifier.create(Mono.just(loadedSurvey))
            .expectNextMatches { loaded ->
                loaded != null && 
                loaded.questions.size == 10 &&
                loaded.questions.all { it.options.size == 5 }
            }
            .verifyComplete()

        val duration = endTime - startTime
        println("설문지 로딩 시간: ${duration}ms")
        println("질문 수: ${loadedSurvey?.questions?.size}")
        println("총 옵션 수: ${loadedSurvey?.questions?.sumOf { it.options.size }}")

        // 성능 기준: 100ms 이하
        assert(duration < 100) { "설문지 로딩이 너무 느림: ${duration}ms" }
    }

    @Test
    fun `배치 처리 성능 테스트`() {
        // Given: 여러 설문지 생성
        val surveys = (1..5).map { createComplexSurvey(5, 3) }
        val savedSurveyIds = surveys.map { saveSurveyWithQuestions(it).block()!!.id }

        // When: 배치로 여러 설문지 로딩
        val startTime = System.currentTimeMillis()
        val loadedSurveys = r2dbcSurveyRepositoryAdapter.findAllWithPaging(
            org.springframework.data.domain.PageRequest.of(0, 10)
        ).block()
        val endTime = System.currentTimeMillis()

        // Then: 성능 검증
        StepVerifier.create(Mono.just(loadedSurveys))
            .expectNextMatches { page ->
                page.content.isNotEmpty()
            }
            .verifyComplete()

        val duration = endTime - startTime
        println("배치 로딩 시간: ${duration}ms")
        println("로딩된 설문지 수: ${loadedSurveys?.content?.size}")

        // 성능 기준: 200ms 이하
        assert(duration < 200) { "배치 로딩이 너무 느림: ${duration}ms" }
    }

    @Test
    fun `동시 요청 성능 테스트`() {
        // Given: 복잡한 설문지 생성
        val survey = createComplexSurvey(8, 4)
        val savedSurvey = saveSurveyWithQuestions(survey).block()!!

        // When: 동시에 여러 요청 처리
        val concurrentRequests = 10
        val latch = CountDownLatch(concurrentRequests)
        val startTime = System.currentTimeMillis()

        repeat(concurrentRequests) {
            Thread {
                r2dbcSurveyRepositoryAdapter.findByIdWithQuestions(savedSurvey.id)
                    .block()
                latch.countDown()
            }.start()
        }

        latch.await(5, TimeUnit.SECONDS)
        val endTime = System.currentTimeMillis()

        // Then: 성능 검증
        val duration = endTime - startTime
        println("동시 요청 처리 시간: ${duration}ms")
        println("동시 요청 수: $concurrentRequests")

        // 성능 기준: 500ms 이하
        assert(duration < 500) { "동시 요청 처리가 너무 느림: ${duration}ms" }
    }

    @Test
    fun `캐시 성능 테스트`() {
        // Given: 설문지 생성
        val survey = createComplexSurvey(5, 3)
        val savedSurvey = saveSurveyWithQuestions(survey).block()!!

        // When: 첫 번째 요청 (캐시 미스)
        val firstStartTime = System.currentTimeMillis()
        val firstLoad = r2dbcSurveyRepositoryAdapter.findByIdWithQuestions(savedSurvey.id).block()
        val firstDuration = System.currentTimeMillis() - firstStartTime

        // 두 번째 요청 (캐시 히트)
        val secondStartTime = System.currentTimeMillis()
        val secondLoad = r2dbcSurveyRepositoryAdapter.findByIdWithQuestions(savedSurvey.id).block()
        val secondDuration = System.currentTimeMillis() - secondStartTime

        // Then: 캐시 효과 검증
        println("첫 번째 요청 시간: ${firstDuration}ms")
        println("두 번째 요청 시간: ${secondDuration}ms")
        println("캐시 효과: ${firstDuration - secondDuration}ms")

        // 캐시 히트가 더 빠르야 함
        assert(secondDuration < firstDuration) { "캐시가 효과적이지 않음" }
    }

    private fun createComplexSurvey(questionCount: Int, optionCount: Int): Survey {
        val questions = (1..questionCount).map { questionIndex ->
            val options = (1..optionCount).map { optionIndex ->
                QuestionOption.create(
                    order = optionIndex,
                    text = "옵션 $optionIndex"
                )
            }

            Question.create(
                surveyId = SurveyId.generate(),
                order = questionIndex,
                text = "질문 $questionIndex",
                description = "질문 $questionIndex 설명",
                type = QuestionType.SINGLE_CHOICE,
                required = questionIndex % 2 == 0,
                options = options.map { it.text }
            )
        }

        return Survey.create(
            title = "성능 테스트 설문지",
            description = "성능 테스트를 위한 설문지",
            createdBy = UserId.generate(),
            settings = SurveySettings()
        ).copy(questions = questions)
    }

    private fun saveSurveyWithQuestions(survey: Survey): Mono<Survey> {
        return r2dbcSurveyRepositoryAdapter.save(survey)
    }
} 
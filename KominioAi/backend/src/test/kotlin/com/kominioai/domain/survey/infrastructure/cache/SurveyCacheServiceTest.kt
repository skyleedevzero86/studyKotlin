package com.kominioai.domain.survey.infrastructure.cache

import com.kominioai.domain.survey.domain.model.SurveySettings
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class SurveyCacheServiceTest : BehaviorSpec({
    
    val redisTemplate = context.getBean(ReactiveRedisTemplate::class.java)
    val surveyCacheService = context.getBean(SurveyCacheService::class.java)
    
    given("설문조사 데이터가 있을 때") {
        val surveyId = SurveyId.generate()
        val survey = Survey(
            id = surveyId,
            title = "테스트 설문조사",
            description = "캐싱 테스트용 설문조사",
            createdBy = UserId.generate(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            status = SurveyStatus.PUBLISHED,
            questions = emptyList(),
            settings = SurveySettings()
        )
        
        `when`("설문조사를 캐시에 저장하면") {
            val result = surveyCacheService.cacheSurvey(survey)
            
            then("성공적으로 저장되어야 한다") {
                StepVerifier.create(result)
                    .expectNext(true)
                    .verifyComplete()
            }
        }
        
        `when`("캐시된 설문조사를 조회하면") {
            val result = surveyCacheService.getSurveyById(surveyId)
            
            then("저장된 설문조사가 반환되어야 한다") {
                StepVerifier.create(result)
                    .expectNextMatches { cachedSurvey ->
                        cachedSurvey != null && cachedSurvey.id == surveyId
                    }
                    .verifyComplete()
            }
        }
        
        `when`("캐시를 무효화하면") {
            val invalidateResult = surveyCacheService.invalidateSurveyCache(surveyId)
            
            then("성공적으로 무효화되어야 한다") {
                StepVerifier.create(invalidateResult)
                    .expectNext(true)
                    .verifyComplete()
            }
            
            `when`("무효화된 캐시를 조회하면") {
                val getResult = surveyCacheService.getSurveyById(surveyId)
                
                then("빈 결과가 반환되어야 한다") {
                    StepVerifier.create(getResult)
                        .verifyComplete()
                }
            }
        }
    }
    
    given("게시된 설문조사 목록이 있을 때") {
        val surveys = listOf(
            Survey(
                id = SurveyId.generate(),
                title = "게시된 설문조사 1",
                description = "첫 번째 게시된 설문조사",
                createdBy = UserId.generate(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                status = SurveyStatus.PUBLISHED,
                questions = emptyList(),
                settings = SurveySettings()
            ),
            Survey(
                id = SurveyId.generate(),
                title = "게시된 설문조사 2",
                description = "두 번째 게시된 설문조사",
                createdBy = UserId.generate(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                status = SurveyStatus.PUBLISHED,
                questions = emptyList(),
                settings = SurveySettings()
            )
        )
        
        `when`("게시된 설문조사 목록을 캐시에 저장하면") {
            val result = surveyCacheService.cachePublishedSurveys(surveys)
            
            then("성공적으로 저장되어야 한다") {
                StepVerifier.create(result)
                    .expectNext(true)
                    .verifyComplete()
            }
        }
        
        `when`("캐시된 게시된 설문조사 목록을 조회하면") {
            val result = surveyCacheService.getPublishedSurveys()
            
            then("저장된 목록이 반환되어야 한다") {
                StepVerifier.create(result)
                    .expectNextMatches { cachedSurveys ->
                        cachedSurveys != null && cachedSurveys.size == surveys.size
                    }
                    .verifyComplete()
            }
        }
        
        `when`("게시된 설문조사 목록 캐시를 무효화하면") {
            val invalidateResult = surveyCacheService.invalidatePublishedSurveysCache()
            
            then("성공적으로 무효화되어야 한다") {
                StepVerifier.create(invalidateResult)
                    .expectNext(true)
                    .verifyComplete()
            }
        }
    }
    
    given("설문조사 통계 데이터가 있을 때") {
        val surveyId = SurveyId.generate()
        val statistics = mapOf(
            "title" to "통계 테스트 설문조사",
            "responseCount" to 100,
            "questionStatistics" to emptyList<Map<String, Any>>()
        )
        
        `when`("설문조사 통계를 캐시에 저장하면") {
            val result = surveyCacheService.cacheSurveyStatistics(surveyId, statistics)
            
            then("성공적으로 저장되어야 한다") {
                StepVerifier.create(result)
                    .expectNext(true)
                    .verifyComplete()
            }
        }
        
        `when`("캐시된 설문조사 통계를 조회하면") {
            val result = surveyCacheService.getSurveyStatistics(surveyId)
            
            then("저장된 통계가 반환되어야 한다") {
                StepVerifier.create(result)
                    .expectNextMatches { cachedStats ->
                        cachedStats != null && cachedStats["title"] == statistics["title"]
                    }
                    .verifyComplete()
            }
        }
    }
    
    given("캐시에 여러 설문조사가 저장되어 있을 때") {
        val survey1 = Survey(
            id = SurveyId.generate(),
            title = "테스트 설문조사 1",
            description = "첫 번째 테스트 설문조사",
            createdBy = UserId.generate(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            status = SurveyStatus.PUBLISHED,
            questions = emptyList(),
            settings = SurveySettings()
        )
        
        val survey2 = Survey(
            id = SurveyId.generate(),
            title = "테스트 설문조사 2",
            description = "두 번째 테스트 설문조사",
            createdBy = UserId.generate(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            status = SurveyStatus.DRAFT,
            questions = emptyList(),
            settings = SurveySettings()
        )
        
        beforeTest {
            surveyCacheService.cacheSurvey(survey1).block()
            surveyCacheService.cacheSurvey(survey2).block()
        }
        
        `when`("전체 설문조사 캐시를 무효화하면") {
            val result = surveyCacheService.invalidateAllSurveyCache()
            
            then("성공적으로 무효화되어야 한다") {
                StepVerifier.create(result)
                    .expectNext(true)
                    .verifyComplete()
            }
            
            `when`("무효화된 캐시를 조회하면") {
                val getResult1 = surveyCacheService.getSurveyById(survey1.id)
                val getResult2 = surveyCacheService.getSurveyById(survey2.id)
                
                then("모든 캐시가 비어있어야 한다") {
                    StepVerifier.create(Mono.zip(getResult1, getResult2))
                        .verifyComplete()
                }
            }
        }
    }
    
    given("캐시 통계를 조회할 때") {
        `when`("캐시 통계를 요청하면") {
            val result = surveyCacheService.getCacheStats()
            
            then("통계 정보가 반환되어야 한다") {
                StepVerifier.create(result)
                    .expectNextMatches { stats ->
                        stats.containsKey("surveyCacheCount") &&
                        stats.containsKey("surveyWithQuestionsCacheCount") &&
                        stats.containsKey("statisticsCacheCount") &&
                        stats.containsKey("hasPublishedSurveysCache")
                    }
                    .verifyComplete()
            }
        }
    }
}) {
    
    companion object {
        @Container
        @JvmStatic
        val redisContainer = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
        
        @JvmStatic
        @DynamicPropertySource
        fun redisProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { redisContainer.host }
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379) }
        }
    }
} 
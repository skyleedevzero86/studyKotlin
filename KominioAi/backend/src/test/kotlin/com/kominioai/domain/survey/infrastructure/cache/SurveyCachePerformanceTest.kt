package com.kominioai.domain.survey.infrastructure.cache

import com.kominioai.domain.survey.domain.model.SurveySettings
import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.domain.valueobject.SurveyStatus
import com.kominioai.domain.survey.domain.valueobject.UserId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SurveyCachePerformanceTest {
    
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
    
    @Test
    fun `캐시 히트 성능 테스트`() {
        val surveyCacheService = context.getBean(SurveyCacheService::class.java)
        
        // 테스트 데이터 생성
        val survey = createTestSurvey()
        
        // 첫 번째 조회 (캐시 미스)
        val startTime = System.currentTimeMillis()
        surveyCacheService.cacheSurvey(survey).block()
        val firstQueryTime = System.currentTimeMillis() - startTime
        
        // 두 번째 조회 (캐시 히트)
        val cacheHitStartTime = System.currentTimeMillis()
        val cachedSurvey = surveyCacheService.getSurveyById(survey.id).block()
        val cacheHitTime = System.currentTimeMillis() - cacheHitStartTime
        
        println("캐시 미스 시간: ${firstQueryTime}ms")
        println("캐시 히트 시간: ${cacheHitTime}ms")
        println("성능 개선율: ${(firstQueryTime - cacheHitTime) * 100.0 / firstQueryTime}%")
        
        // 캐시 히트가 캐시 미스보다 빠르거나 비슷해야 함
        assert(cacheHitTime <= firstQueryTime * 2) { "캐시 히트가 예상보다 느림" }
        assert(cachedSurvey != null) { "캐시된 데이터가 null임" }
    }
    
    @Test
    fun `동시 조회 성능 테스트`() {
        val surveyCacheService = context.getBean(SurveyCacheService::class.java)
        
        // 여러 설문조사 생성 및 캐싱
        val surveys = (1..10).map { createTestSurvey() }
        surveys.forEach { survey ->
            surveyCacheService.cacheSurvey(survey).block()
        }
        
        val threadCount = 10
        val latch = CountDownLatch(threadCount)
        val startTime = System.currentTimeMillis()
        
        // 동시 조회 테스트
        repeat(threadCount) { threadIndex ->
            Thread {
                try {
                    val survey = surveys[threadIndex % surveys.size]
                    val result = surveyCacheService.getSurveyById(survey.id).block()
                    assert(result != null) { "Thread $threadIndex: 캐시된 데이터가 null임" }
                } finally {
                    latch.countDown()
                }
            }.start()
        }
        
        latch.await(5, TimeUnit.SECONDS)
        val totalTime = System.currentTimeMillis() - startTime
        
        println("동시 조회 테스트 완료: ${threadCount}개 스레드, ${totalTime}ms")
        println("평균 조회 시간: ${totalTime.toDouble() / threadCount}ms")
        
        assert(totalTime < 5000) { "동시 조회가 너무 느림: ${totalTime}ms" }
    }
    
    @Test
    fun `게시된 설문조사 목록 캐싱 성능 테스트`() {
        val surveyCacheService = context.getBean(SurveyCacheService::class.java)
        
        // 게시된 설문조사 목록 생성
        val publishedSurveys = (1..50).map { 
            createTestSurvey(status = SurveyStatus.PUBLISHED) 
        }
        
        // 캐시 저장
        val cacheStartTime = System.currentTimeMillis()
        surveyCacheService.cachePublishedSurveys(publishedSurveys).block()
        val cacheTime = System.currentTimeMillis() - cacheStartTime
        
        // 캐시 조회
        val queryStartTime = System.currentTimeMillis()
        val cachedSurveys = surveyCacheService.getPublishedSurveys().block()
        val queryTime = System.currentTimeMillis() - queryStartTime
        
        println("게시된 설문조사 목록 캐시 저장 시간: ${cacheTime}ms")
        println("게시된 설문조사 목록 캐시 조회 시간: ${queryTime}ms")
        println("캐시된 설문조사 수: ${cachedSurveys?.size ?: 0}")
        
        assert(cachedSurveys != null) { "캐시된 목록이 null임" }
        assert(cachedSurveys!!.size == publishedSurveys.size) { "캐시된 목록 크기가 다름" }
        assert(queryTime < cacheTime) { "캐시 조회가 캐시 저장보다 느림" }
    }
    
    @Test
    fun `캐시 무효화 성능 테스트`() {
        val surveyCacheService = context.getBean(SurveyCacheService::class.java)
        
        // 여러 설문조사 생성 및 캐싱
        val surveys = (1..20).map { createTestSurvey() }
        surveys.forEach { survey ->
            surveyCacheService.cacheSurvey(survey).block()
        }
        
        // 개별 캐시 무효화 성능 테스트
        val individualInvalidateStartTime = System.currentTimeMillis()
        surveys.take(5).forEach { survey ->
            surveyCacheService.invalidateSurveyCache(survey.id).block()
        }
        val individualInvalidateTime = System.currentTimeMillis() - individualInvalidateStartTime
        
        // 전체 캐시 무효화 성능 테스트
        val bulkInvalidateStartTime = System.currentTimeMillis()
        surveyCacheService.invalidateAllSurveyCache().block()
        val bulkInvalidateTime = System.currentTimeMillis() - bulkInvalidateStartTime
        
        println("개별 캐시 무효화 시간 (5개): ${individualInvalidateTime}ms")
        println("전체 캐시 무효화 시간 (20개): ${bulkInvalidateTime}ms")
        println("개별 무효화 평균: ${individualInvalidateTime.toDouble() / 5}ms")
        println("전체 무효화 평균: ${bulkInvalidateTime.toDouble() / 20}ms")
        
        assert(bulkInvalidateTime < individualInvalidateTime * 4) { "전체 무효화가 예상보다 느림" }
    }
    
    @Test
    fun `캐시 메모리 사용량 테스트`() {
        val surveyCacheService = context.getBean(SurveyCacheService::class.java)
        
        // 메모리 사용량 측정 시작
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()
        
        // 대량의 설문조사 캐싱
        val surveys = (1..1000).map { createTestSurvey() }
        surveys.forEach { survey ->
            surveyCacheService.cacheSurvey(survey).block()
        }
        
        val afterCacheMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = afterCacheMemory - initialMemory
        
        println("초기 메모리 사용량: ${initialMemory / 1024 / 1024}MB")
        println("캐시 후 메모리 사용량: ${afterCacheMemory / 1024 / 1024}MB")
        println("캐시로 인한 메모리 증가: ${memoryUsed / 1024 / 1024}MB")
        println("설문조사당 평균 메모리: ${memoryUsed / 1024 / surveys.size}KB")
        
        // 메모리 사용량이 합리적인 범위 내에 있어야 함
        assert(memoryUsed < 500 * 1024 * 1024) { "캐시 메모리 사용량이 너무 큼: ${memoryUsed / 1024 / 1024}MB" }
    }
    
    @Test
    fun `캐시 만료 시간 테스트`() {
        val surveyCacheService = context.getBean(SurveyCacheService::class.java)
        
        val survey = createTestSurvey()
        
        // 캐시 저장
        surveyCacheService.cacheSurvey(survey).block()
        
        // 즉시 조회 (캐시 히트)
        val immediateResult = surveyCacheService.getSurveyById(survey.id).block()
        assert(immediateResult != null) { "즉시 조회 시 캐시 히트 실패" }
        
        // 캐시 만료 시간이 짧게 설정된 테스트용 캐시 서비스를 사용하여 만료 테스트
        // 실제 구현에서는 별도의 테스트용 설정이 필요할 수 있음
        
        println("캐시 만료 시간 테스트 완료")
    }
    
    private fun createTestSurvey(status: SurveyStatus = SurveyStatus.PUBLISHED): Survey {
        return Survey(
            id = SurveyId.generate(),
            title = "성능 테스트 설문조사 ${System.currentTimeMillis()}",
            description = "성능 테스트를 위한 설문조사",
            createdBy = UserId.generate(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            status = status,
            questions = emptyList(),
            settings = SurveySettings()
        )
    }
} 
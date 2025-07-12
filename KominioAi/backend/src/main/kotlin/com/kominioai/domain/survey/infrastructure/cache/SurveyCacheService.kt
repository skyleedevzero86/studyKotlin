package com.kominioai.domain.survey.infrastructure.cache

import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.global.config.RedisConfiguration
import com.kominioai.global.service.BusinessMetricsService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
@ConditionalOnProperty(name = ["spring.data.redis.host"]) // Redis가 설정된 경우에만 활성화
class SurveyCacheService(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
    private val businessMetricsService: BusinessMetricsService
) {

    private val logger = LoggerFactory.getLogger(SurveyCacheService::class.java)

    /**
     * 설문조사 단일 조회 캐싱
     */
    fun getSurveyById(surveyId: SurveyId): Mono<Survey?> {
        val cacheKey = "${RedisConfiguration.SURVEY_CACHE_PREFIX}${surveyId.value}"
        val startTime = System.currentTimeMillis()

        return redisTemplate.opsForValue().get(cacheKey)
            .map { it as Survey }
            .doOnSuccess { survey ->
                val duration = System.currentTimeMillis() - startTime
                if (survey != null) {
                    logger.debug("Cache hit for survey: ${surveyId.value}")
                    businessMetricsService.recordCacheHit("survey", surveyId.value)
                    businessMetricsService.recordRedisOperation("get", duration, true)
                } else {
                    logger.debug("Cache miss for survey: ${surveyId.value}")
                    businessMetricsService.recordCacheMiss("survey", surveyId.value)
                    businessMetricsService.recordRedisOperation("get", duration, true)
                }
            }
            .onErrorResume { error ->
                val duration = System.currentTimeMillis() - startTime
                logger.warn("Error reading from cache for survey: ${surveyId.value}, error: ${error.message}")
                businessMetricsService.recordRedisOperation("get", duration, false)
                Mono.empty()
            }
    }

    /**
     * 설문조사 단일 저장 캐싱
     */
    fun cacheSurvey(survey: Survey): Mono<Boolean> {
        val cacheKey = "${RedisConfiguration.SURVEY_CACHE_PREFIX}${survey.id.value}"
        val startTime = System.currentTimeMillis()

        return redisTemplate.opsForValue()
            .set(cacheKey, survey, RedisConfiguration.SURVEY_CACHE_TTL)
            .doOnSuccess {
                val duration = System.currentTimeMillis() - startTime
                logger.debug("Survey cached: ${survey.id.value}")
                businessMetricsService.recordRedisOperation("set", duration, true)
            }
            .onErrorResume { error ->
                val duration = System.currentTimeMillis() - startTime
                logger.warn("Error caching survey: ${survey.id.value}, error: ${error.message}")
                businessMetricsService.recordRedisOperation("set", duration, false)
                Mono.just(false)
            }
    }

    /**
     * 설문조사 (질문 포함) 단일 조회 캐싱
     */
    fun getSurveyWithQuestionsById(surveyId: SurveyId): Mono<Survey?> {
        val cacheKey = "${RedisConfiguration.SURVEY_WITH_QUESTIONS_CACHE_PREFIX}${surveyId.value}"
        val startTime = System.currentTimeMillis()

        return redisTemplate.opsForValue().get(cacheKey)
            .map { it as Survey }
            .doOnSuccess { survey ->
                val duration = System.currentTimeMillis() - startTime
                if (survey != null) {
                    logger.debug("Cache hit for survey with questions: ${surveyId.value}")
                    businessMetricsService.recordCacheHit("survey_with_questions", surveyId.value)
                    businessMetricsService.recordRedisOperation("get", duration, true)
                } else {
                    logger.debug("Cache miss for survey with questions: ${surveyId.value}")
                    businessMetricsService.recordCacheMiss("survey_with_questions", surveyId.value)
                    businessMetricsService.recordRedisOperation("get", duration, true)
                }
            }
            .onErrorResume { error ->
                val duration = System.currentTimeMillis() - startTime
                logger.warn("Error reading from cache for survey with questions: ${surveyId.value}, error: ${error.message}")
                businessMetricsService.recordRedisOperation("get", duration, false)
                Mono.empty()
            }
    }

    /**
     * 설문조사 (질문 포함) 단일 저장 캐싱
     */
    fun cacheSurveyWithQuestions(survey: Survey): Mono<Boolean> {
        val cacheKey = "${RedisConfiguration.SURVEY_WITH_QUESTIONS_CACHE_PREFIX}${survey.id.value}"
        val startTime = System.currentTimeMillis()

        return redisTemplate.opsForValue()
            .set(cacheKey, survey, RedisConfiguration.SURVEY_WITH_QUESTIONS_CACHE_TTL)
            .doOnSuccess {
                val duration = System.currentTimeMillis() - startTime
                logger.debug("Survey with questions cached: ${survey.id.value}")
                businessMetricsService.recordRedisOperation("set", duration, true)
            }
            .onErrorResume { error ->
                val duration = System.currentTimeMillis() - startTime
                logger.warn("Error caching survey with questions: ${survey.id.value}, error: ${error.message}")
                businessMetricsService.recordRedisOperation("set", duration, false)
                Mono.just(false)
            }
    }

    /**
     * 게시된 설문조사 목록 조회 캐싱
     */
    fun getPublishedSurveys(): Mono<List<Survey>?> {
        val startTime = System.currentTimeMillis()

        return redisTemplate.opsForList()
            .range(RedisConfiguration.PUBLISHED_SURVEYS_CACHE_KEY, 0, -1)
            .collectList()
            .map { list ->
                if (list.isNotEmpty()) {
                    list.map { it as Survey }
                } else {
                    null
                }
            }
            .doOnSuccess { surveys ->
                val duration = System.currentTimeMillis() - startTime
                if (surveys != null) {
                    logger.debug("Cache hit for published surveys, count: ${surveys.size}")
                    businessMetricsService.recordCacheHit("published_surveys", "list")
                    businessMetricsService.recordRedisOperation("get", duration, true)
                } else {
                    logger.debug("Cache miss for published surveys")
                    businessMetricsService.recordCacheMiss("published_surveys", "list")
                    businessMetricsService.recordRedisOperation("get", duration, true)
                }
            }
            .onErrorResume { error ->
                val duration = System.currentTimeMillis() - startTime
                logger.warn("Error reading from cache for published surveys, error: ${error.message}")
                businessMetricsService.recordRedisOperation("get", duration, false)
                Mono.empty()
            }
    }

    /**
     * 게시된 설문조사 목록 저장 캐싱
     */
    fun cachePublishedSurveys(surveys: List<Survey>): Mono<Boolean> {
        val startTime = System.currentTimeMillis()

        return redisTemplate.delete(RedisConfiguration.PUBLISHED_SURVEYS_CACHE_KEY)
            .then(
                if (surveys.isNotEmpty()) {
                    val surveyArray = surveys.toTypedArray<Survey>()
                    redisTemplate.opsForList()
                        .leftPushAll(RedisConfiguration.PUBLISHED_SURVEYS_CACHE_KEY, *surveyArray)
                        .then(redisTemplate.expire(RedisConfiguration.PUBLISHED_SURVEYS_CACHE_KEY, RedisConfiguration.PUBLISHED_SURVEYS_CACHE_TTL))
                } else {
                    Mono.just(true)
                }
            )
            .map { true }
            .doOnSuccess {
                val duration = System.currentTimeMillis() - startTime
                logger.debug("Published surveys cached, count: ${surveys.size}")
                businessMetricsService.recordRedisOperation("set", duration, true)
            }
            .onErrorResume { error ->
                val duration = System.currentTimeMillis() - startTime
                logger.warn("Error caching published surveys, error: ${error.message}")
                businessMetricsService.recordRedisOperation("set", duration, false)
                Mono.just(false)
            }
    }

    /**
     * 설문조사 통계 조회 캐싱
     */
    fun getSurveyStatistics(surveyId: SurveyId): Mono<Map<String, Any>?> {
        val cacheKey = "${RedisConfiguration.SURVEY_STATISTICS_CACHE_PREFIX}${surveyId.value}"
        val startTime = System.currentTimeMillis()

        return redisTemplate.opsForValue().get(cacheKey)
            .map { it as Map<String, Any> }
            .doOnSuccess { stats ->
                val duration = System.currentTimeMillis() - startTime
                if (stats != null) {
                    logger.debug("Cache hit for survey statistics: ${surveyId.value}")
                    businessMetricsService.recordCacheHit("survey_statistics", surveyId.value)
                    businessMetricsService.recordRedisOperation("get", duration, true)
                } else {
                    logger.debug("Cache miss for survey statistics: ${surveyId.value}")
                    businessMetricsService.recordCacheMiss("survey_statistics", surveyId.value)
                    businessMetricsService.recordRedisOperation("get", duration, true)
                }
            }
            .onErrorResume { error ->
                val duration = System.currentTimeMillis() - startTime
                logger.warn("Error reading from cache for survey statistics: ${surveyId.value}, error: ${error.message}")
                businessMetricsService.recordRedisOperation("get", duration, false)
                Mono.empty()
            }
    }

    /**
     * 설문조사 통계 저장 캐싱
     */
    fun cacheSurveyStatistics(surveyId: SurveyId, statistics: Map<String, Any>): Mono<Boolean> {
        val cacheKey = "${RedisConfiguration.SURVEY_STATISTICS_CACHE_PREFIX}${surveyId.value}"
        val startTime = System.currentTimeMillis()

        return redisTemplate.opsForValue()
            .set(cacheKey, statistics, RedisConfiguration.SURVEY_STATISTICS_CACHE_TTL)
            .doOnSuccess {
                val duration = System.currentTimeMillis() - startTime
                logger.debug("Survey statistics cached: ${surveyId.value}")
                businessMetricsService.recordRedisOperation("set", duration, true)
            }
            .onErrorResume { error ->
                val duration = System.currentTimeMillis() - startTime
                logger.warn("Error caching survey statistics: ${surveyId.value}, error: ${error.message}")
                businessMetricsService.recordRedisOperation("set", duration, false)
                Mono.just(false)
            }
    }

    /**
     * 설문조사 캐시 무효화
     */
    fun invalidateSurveyCache(surveyId: SurveyId): Mono<Boolean> {
        val surveyKey = "${RedisConfiguration.SURVEY_CACHE_PREFIX}${surveyId.value}"
        val surveyWithQuestionsKey = "${RedisConfiguration.SURVEY_WITH_QUESTIONS_CACHE_PREFIX}${surveyId.value}"
        val statisticsKey = "${RedisConfiguration.SURVEY_STATISTICS_CACHE_PREFIX}${surveyId.value}"
        val startTime = System.currentTimeMillis()

        return Mono.zip(
            redisTemplate.delete(surveyKey),
            redisTemplate.delete(surveyWithQuestionsKey),
            redisTemplate.delete(statisticsKey)
        )
        .map { true }
        .doOnSuccess { _: Boolean ->
            val duration = System.currentTimeMillis() - startTime
            logger.debug("Survey cache invalidated: ${surveyId.value}")
            businessMetricsService.recordRedisOperation("delete", duration, true)
        }
        .onErrorResume { error ->
            val duration = System.currentTimeMillis() - startTime
            logger.warn("Error invalidating survey cache: ${surveyId.value}, error: ${error.message}")
            businessMetricsService.recordRedisOperation("delete", duration, false)
            Mono.just(false)
        }
    }

    /**
     * 게시된 설문조사 목록 캐시 무효화
     */
    fun invalidatePublishedSurveysCache(): Mono<Boolean> {
        return redisTemplate.delete(RedisConfiguration.PUBLISHED_SURVEYS_CACHE_KEY)
            .map { true }
            .doOnSuccess {
                logger.debug("Published surveys cache invalidated")
            }
            .onErrorResume { error ->
                logger.warn("Error invalidating published surveys cache, error: ${error.message}")
                Mono.just(false)
            }
    }

    /**
     * 전체 설문조사 캐시 무효화
     */
    fun invalidateAllSurveyCache(): Mono<Boolean> {
        val pattern = "${RedisConfiguration.SURVEY_CACHE_PREFIX}*"
        val startTime = System.currentTimeMillis()
        
        return redisTemplate.keys(pattern)
            .collectList()
            .flatMap { keys: List<String> ->
                if (keys.isNotEmpty()) {
                    redisTemplate.delete(*keys.toTypedArray())
                        .map { deletedCount ->
                            logger.debug("Deleted $deletedCount keys from survey cache")
                            deletedCount
                        }
                } else {
                    Mono.just(0L)
                }
            }
            .map { true }
            .doOnSuccess { _: Boolean ->
                val duration = System.currentTimeMillis() - startTime
                logger.debug("All survey cache invalidated in ${duration}ms")
                businessMetricsService.recordRedisOperation("delete", duration, true)
            }
            .onErrorResume { error: Throwable ->
                val duration = System.currentTimeMillis() - startTime
                logger.warn("Error invalidating all survey cache, error: ${error.message}")
                businessMetricsService.recordRedisOperation("delete", duration, false)
                Mono.just(false)
            }
    }

    /**
     * 캐시 통계 조회
     */
    fun getCacheStats(): Mono<Map<String, Any>> {
        return Mono.zip(
            redisTemplate.keys("${RedisConfiguration.SURVEY_CACHE_PREFIX}*").count(),
            redisTemplate.keys("${RedisConfiguration.SURVEY_WITH_QUESTIONS_CACHE_PREFIX}*").count(),
            redisTemplate.keys("${RedisConfiguration.SURVEY_STATISTICS_CACHE_PREFIX}*").count(),
            redisTemplate.hasKey(RedisConfiguration.PUBLISHED_SURVEYS_CACHE_KEY)
        ).map { tuple ->
            val surveyCount = tuple.t1
            val surveyWithQuestionsCount = tuple.t2
            val statisticsCount = tuple.t3
            val hasPublishedSurveys = tuple.t4

            mapOf(
                "surveyCacheCount" to surveyCount,
                "surveyWithQuestionsCacheCount" to surveyWithQuestionsCount,
                "statisticsCacheCount" to statisticsCount,
                "hasPublishedSurveysCache" to hasPublishedSurveys
            )
        }
    }
} 
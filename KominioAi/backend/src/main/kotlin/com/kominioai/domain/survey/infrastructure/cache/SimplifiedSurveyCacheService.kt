package com.kominioai.domain.survey.infrastructure.cache

import com.kominioai.domain.survey.domain.model.domain.Survey
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.global.config.RedisConfiguration
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Duration

@Service
@ConditionalOnProperty(name = ["spring.data.redis.host"])
class SimplifiedSurveyCacheService(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>
) {

    private val logger = LoggerFactory.getLogger(SimplifiedSurveyCacheService::class.java)

    companion object {
        private const val SURVEY_CACHE_TTL_MINUTES = 30L
        private const val SURVEY_STATISTICS_CACHE_TTL_MINUTES = 15L
        private const val PUBLISHED_SURVEYS_CACHE_TTL_MINUTES = 10L
    }

    fun getSurveyWithQuestions(surveyId: SurveyId): Mono<Survey?> {
        val cacheKey = "survey:with-questions:${surveyId.value}"

        return redisTemplate.opsForValue().get(cacheKey)
            .map { it as Survey }
            .doOnSuccess { survey ->
                if (survey != null) {
                    logger.debug("Cache hit for survey: ${surveyId.value}")
                } else {
                    logger.debug("Cache miss for survey: ${surveyId.value}")
                }
            }
            .onErrorResume { error ->
                logger.warn("Error reading from cache for survey: ${surveyId.value}, error: ${error.message}")
                Mono.empty()
            }
    }

    fun cacheSurveyWithQuestions(survey: Survey): Mono<Boolean> {
        val cacheKey = "survey:with-questions:${survey.id.value}"

        return redisTemplate.opsForValue()
            .set(cacheKey, survey, Duration.ofMinutes(SURVEY_CACHE_TTL_MINUTES))
            .doOnSuccess {
                logger.debug("Survey cached: ${survey.id.value}")
            }
            .onErrorResume { error ->
                logger.warn("Error caching survey: ${survey.id.value}, error: ${error.message}")
                Mono.just(false)
            }
    }

    fun getPublishedSurveys(): Mono<List<Survey>?> {
        val cacheKey = "surveys:published"

        return redisTemplate.opsForList()
            .range(cacheKey, 0, -1)
            .collectList()
            .map { list ->
                if (list.isNotEmpty()) {
                    list.map { it as Survey }
                } else {
                    null
                }
            }
            .doOnSuccess { surveys ->
                if (surveys != null) {
                    logger.debug("Cache hit for published surveys, count: ${surveys.size}")
                } else {
                    logger.debug("Cache miss for published surveys")
                }
            }
            .onErrorResume { error ->
                logger.warn("Error reading from cache for published surveys, error: ${error.message}")
                Mono.empty()
            }
    }

    fun cachePublishedSurveys(surveys: List<Survey>): Mono<Boolean> {
        val cacheKey = "surveys:published"

        return redisTemplate.delete(cacheKey)
            .then(
                if (surveys.isNotEmpty()) {
                    val surveyArray = surveys.toTypedArray<Survey>()
                    redisTemplate.opsForList()
                        .leftPushAll(cacheKey, *surveyArray)
                        .then(redisTemplate.expire(cacheKey, Duration.ofMinutes(PUBLISHED_SURVEYS_CACHE_TTL_MINUTES)))
                } else {
                    Mono.just(true)
                }
            )
            .map { true }
            .doOnSuccess {
                logger.debug("Published surveys cached, count: ${surveys.size}")
            }
            .onErrorResume { error ->
                logger.warn("Error caching published surveys, error: ${error.message}")
                Mono.just(false)
            }
    }

    fun getSurveyStatistics(surveyId: SurveyId): Mono<Map<String, Any>?> {
        val cacheKey = "survey:statistics:${surveyId.value}"

        return redisTemplate.opsForValue().get(cacheKey)
            .map { it as Map<String, Any> }
            .doOnSuccess { stats ->
                if (stats != null) {
                    logger.debug("Cache hit for survey statistics: ${surveyId.value}")
                } else {
                    logger.debug("Cache miss for survey statistics: ${surveyId.value}")
                }
            }
            .onErrorResume { error ->
                logger.warn("Error reading from cache for survey statistics: ${surveyId.value}, error: ${error.message}")
                Mono.empty()
            }
    }

    fun cacheSurveyStatistics(surveyId: SurveyId, statistics: Map<String, Any>): Mono<Boolean> {
        val cacheKey = "survey:statistics:${surveyId.value}"

        return redisTemplate.opsForValue()
            .set(cacheKey, statistics, Duration.ofMinutes(SURVEY_STATISTICS_CACHE_TTL_MINUTES))
            .doOnSuccess {
                logger.debug("Survey statistics cached: ${surveyId.value}")
            }
            .onErrorResume { error ->
                logger.warn("Error caching survey statistics: ${surveyId.value}, error: ${error.message}")
                Mono.just(false)
            }
    }

    fun invalidateSurveyCache(surveyId: SurveyId): Mono<Boolean> {
        val surveyKey = "survey:with-questions:${surveyId.value}"
        val statisticsKey = "survey:statistics:${surveyId.value}"

        return Mono.zip(
            redisTemplate.delete(surveyKey),
            redisTemplate.delete(statisticsKey)
        )
            .map { true }
            .doOnSuccess {
                logger.debug("Survey cache invalidated: ${surveyId.value}")
            }
            .onErrorResume { error ->
                logger.warn("Error invalidating survey cache: ${surveyId.value}, error: ${error.message}")
                Mono.just(false)
            }
    }

    fun invalidatePublishedSurveysCache(): Mono<Boolean> {
        return redisTemplate.delete("surveys:published")
            .map { true }
            .doOnSuccess {
                logger.debug("Published surveys cache invalidated")
            }
            .onErrorResume { error ->
                logger.warn("Error invalidating published surveys cache, error: ${error.message}")
                Mono.just(false)
            }
    }
}
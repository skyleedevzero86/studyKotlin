package com.kominioai.domain.survey.infrastructure.cache

import com.kominioai.domain.survey.application.port.output.SurveyStatisticsRepository
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.presentation.rest.dto.response.SurveyStatisticsDto
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Duration

@Service
@ConditionalOnProperty(name = ["spring.data.redis.host"])
class OptimizedSurveyStatisticsCacheService(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
    private val surveyStatisticsRepository: SurveyStatisticsRepository
) {

    private val logger = LoggerFactory.getLogger(OptimizedSurveyStatisticsCacheService::class.java)

    companion object {
        private const val STATISTICS_CACHE_TTL_MINUTES = 10L
        private const val STATISTICS_CACHE_PREFIX = "survey:statistics:"
    }
    fun getSurveyStatistics(surveyId: SurveyId): Mono<SurveyStatisticsDto> {
        val cacheKey = "$STATISTICS_CACHE_PREFIX${surveyId.value}"

        return redisTemplate.opsForValue().get(cacheKey)
            .map { it as SurveyStatisticsDto }
            .doOnSuccess { statistics ->
                if (statistics != null) {
                    logger.debug("Cache hit for survey statistics: ${surveyId.value}")
                } else {
                    logger.debug("Cache miss for survey statistics: ${surveyId.value}")
                }
            }
            .onErrorResume { error ->
                logger.warn("Error reading from cache for survey statistics: ${surveyId.value}, error: ${error.message}")
                Mono.empty()
            }
            .switchIfEmpty(
                surveyStatisticsRepository.getSurveyStatistics(surveyId)
                    .flatMap { statistics ->
                        cacheSurveyStatistics(surveyId, statistics)
                            .thenReturn(statistics)
                    }
            )
    }

    private fun cacheSurveyStatistics(surveyId: SurveyId, statistics: SurveyStatisticsDto): Mono<Boolean> {
        val cacheKey = "$STATISTICS_CACHE_PREFIX${surveyId.value}"

        return redisTemplate.opsForValue()
            .set(cacheKey, statistics, Duration.ofMinutes(STATISTICS_CACHE_TTL_MINUTES))
            .doOnSuccess {
                logger.debug("Survey statistics cached: ${surveyId.value}")
            }
            .onErrorResume { error ->
                logger.warn("Error caching survey statistics: ${surveyId.value}, error: ${error.message}")
                Mono.just(false)
            }
    }

    fun invalidateSurveyStatisticsCache(surveyId: SurveyId): Mono<Boolean> {
        val cacheKey = "$STATISTICS_CACHE_PREFIX${surveyId.value}"

        return redisTemplate.delete(cacheKey)
            .map { true }
            .doOnSuccess {
                logger.debug("Survey statistics cache invalidated: ${surveyId.value}")
            }
            .onErrorResume { error ->
                logger.warn("Error invalidating survey statistics cache: ${surveyId.value}, error: ${error.message}")
                Mono.just(false)
            }
    }

    fun refreshSurveyStatisticsCache(surveyId: SurveyId): Mono<SurveyStatisticsDto> {
        return surveyStatisticsRepository.refreshSurveyStatistics(surveyId)
            .flatMap { statistics ->
                cacheSurveyStatistics(surveyId, statistics)
                    .thenReturn(statistics)
            }
    }
}
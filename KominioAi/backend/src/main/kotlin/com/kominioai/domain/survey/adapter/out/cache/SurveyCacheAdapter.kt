package com.kominioai.domain.survey.adapter.out.cache

import com.kominioai.domain.survey.application.port.out.CacheSurveyPort
import com.kominioai.domain.survey.domain.model.Survey
import com.kominioai.domain.survey.domain.model.SurveyId
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
class SurveyCacheAdapter(
    private val redisTemplate: ReactiveRedisTemplate<String, SurveyCacheEntity>
) : CacheSurveyPort {

    override fun cacheSurvey(survey: Survey): Mono<Boolean> =
        redisTemplate.opsForValue()
            .set("survey:${survey.id.value}", SurveyCacheEntity.fromDomain(survey))
            .map { true }

    override fun getCachedSurvey(surveyId: SurveyId): Mono<Survey?> =
        redisTemplate.opsForValue()
            .get("survey:${surveyId.value}")
            .map { entity -> entity.toDomain() }

    override fun invalidateSurveyCache(surveyId: SurveyId): Mono<Boolean> =
        redisTemplate.delete("survey:${surveyId.value}")
            .map { it > 0 }

    override fun cacheSurveyList(surveys: List<Survey>): Mono<Boolean> =
        Flux.fromIterable(surveys)
            .flatMap { survey ->
                redisTemplate.opsForValue()
                    .set("survey:${survey.id.value}", SurveyCacheEntity.fromDomain(survey))
            }
            .collectList()
            .map { true }

    override fun getCachedSurveyList(): Mono<List<Survey>?> =
        redisTemplate.keys("survey:*")
            .flatMap { key ->
                redisTemplate.opsForValue().get(key)
            }
            .collectList()
            .map { entities ->
                entities.mapNotNull { it.toDomain() }
            }

    private fun SurveyCacheEntity.toDomain(): Survey? {
        return try {
            Survey.reconstruct(
                id = id ?: return null,
                title = title,
                author = author,
                status = status,
                startDate = startDate ?: LocalDateTime.now(),
                endDate = endDate ?: LocalDateTime.now(),
                participantCount = participantCount,
                targetType = targetType,
                surveyType = surveyType,
                participantType = participantType,
                timeLimit = null,
                questions = emptyList(),
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            null
        }
    }
} 
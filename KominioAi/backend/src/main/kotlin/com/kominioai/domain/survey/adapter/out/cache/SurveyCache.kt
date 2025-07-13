package com.kominioai.domain.survey.adapter.out.cache

import com.kominioai.domain.survey.domain.model.Survey
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class SurveyCache(
    private val redisTemplate: ReactiveRedisTemplate<String, SurveyCacheEntity>
) {
    fun getSurvey(id: Long): Mono<Survey?> =
        redisTemplate.opsForValue().get("survey:$id")
            .map { it.toDomain() }

    fun cacheSurvey(survey: Survey): Mono<Boolean> =
        redisTemplate.opsForValue().set("survey:${survey.id}", SurveyCacheEntity.fromDomain(survey))

    fun deleteSurvey(id: Long): Mono<Boolean> =
        redisTemplate.delete("survey:$id")
            .map { it > 0 }

    fun exists(id: Long): Mono<Boolean> =
        redisTemplate.hasKey("survey:$id")

    fun findByTitle(title: String): Flux<Survey> =
        redisTemplate.keys("survey:*")
            .flatMap { key ->
                redisTemplate.opsForValue().get(key)
            }
            .filter { it.title.contains(title, ignoreCase = true) }
            .map { it.toDomain() }

    fun findByStatus(status: String): Flux<Survey> =
        redisTemplate.keys("survey:*")
            .flatMap { key ->
                redisTemplate.opsForValue().get(key)
            }
            .filter { it.status == status }
            .map { it.toDomain() }

    fun findByAuthor(author: String): Flux<Survey> =
        redisTemplate.keys("survey:*")
            .flatMap { key ->
                redisTemplate.opsForValue().get(key)
            }
            .filter { it.author == author }
            .map { it.toDomain() }
}
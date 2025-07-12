package com.kominioai.domain.survey.infrastructure.cache

import com.kominioai.domain.survey.application.model.Survey
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SurveyCache(
    private val redisTemplate: ReactiveRedisTemplate<String, Survey>
) {
    fun getSurvey(id: Long): Mono<Survey?> =
        redisTemplate.opsForValue().get("survey:$id")

    fun cacheSurvey(survey: Survey): Mono<Boolean> =
        redisTemplate.opsForValue().set("survey:${survey.id}", survey)
}
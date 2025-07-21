package com.kominioai.domain.survey.adapter.out.cache

import com.kominioai.domain.survey.application.port.out.CacheQuizParticipationPort
import com.kominioai.domain.survey.domain.model.ParticipationId
import com.kominioai.domain.survey.domain.model.QuizParticipation
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

import java.time.Duration

@Component
class QuizParticipationCacheAdapter(
    @Qualifier("reactiveObjectRedisTemplate")
    private val redisTemplate: ReactiveRedisTemplate<String, Any>
) : CacheQuizParticipationPort {

    override fun cacheParticipation(participation: QuizParticipation): Mono<Boolean> =
        redisTemplate.opsForValue()
            .set(
                "quiz-participation:${participation.id.value}",
                QuizParticipationCacheEntity.fromDomain(participation),
                Duration.ofMinutes(30)
            )
            .map { true }

    override fun getCachedParticipation(participationId: ParticipationId): Mono<QuizParticipation?> =
        redisTemplate.opsForValue()
            .get("quiz-participation:${participationId.value}")
            .cast(QuizParticipationCacheEntity::class.java)
            .map { entity -> entity.toDomain() }
            .onErrorResume { Mono.empty<QuizParticipation>() }
            .switchIfEmpty(Mono.empty<QuizParticipation>())

    override fun invalidateParticipationCache(participationId: ParticipationId): Mono<Boolean> =
        redisTemplate.delete("quiz-participation:${participationId.value}")
            .map { it > 0 }

    override fun cacheParticipationList(participations: List<QuizParticipation>): Mono<Boolean> =
        Flux.fromIterable(participations)
            .flatMap { participation ->
                redisTemplate.opsForValue()
                    .set(
                        "quiz-participation:${participation.id.value}",
                        QuizParticipationCacheEntity.fromDomain(participation),
                        Duration.ofMinutes(30)
                    )
            }
            .then(Mono.just(true))
}
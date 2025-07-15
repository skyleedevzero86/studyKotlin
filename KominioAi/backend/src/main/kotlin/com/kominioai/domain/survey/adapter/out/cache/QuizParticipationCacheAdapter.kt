package com.kominioai.domain.survey.adapter.out.cache

import com.kominioai.domain.survey.application.port.out.CacheQuizParticipationPort
import com.kominioai.domain.survey.domain.model.ParticipationId
import com.kominioai.domain.survey.domain.model.QuizParticipation
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class QuizParticipationCacheAdapter(
    private val redisTemplate: ReactiveRedisTemplate<String, QuizParticipationCacheEntity>
) : CacheQuizParticipationPort {

    override fun cacheParticipation(participation: QuizParticipation): Mono<Boolean> =
        redisTemplate.opsForValue()
            .set("quiz-participation:${participation.id.value}", QuizParticipationCacheEntity.fromDomain(participation), Duration.ofMinutes(30))
            .map { true }

    override fun getCachedParticipation(participationId: ParticipationId): Mono<QuizParticipation?> =
        redisTemplate.opsForValue()
            .get("quiz-participation:${participationId.value}")
            .map { entity -> entity.toDomain() }

    override fun invalidateParticipationCache(participationId: ParticipationId): Mono<Boolean> =
        redisTemplate.delete("quiz-participation:${participationId.value}")
            .map { it > 0 }

    override fun cacheParticipationList(participations: List<QuizParticipation>): Mono<Boolean> =
        reactor.core.publisher.Flux.fromIterable(participations)
            .flatMap { participation ->
                redisTemplate.opsForValue()
                    .set("quiz-participation:${participation.id.value}", QuizParticipationCacheEntity.fromDomain(participation), Duration.ofMinutes(30))
            }
            .collectList()
            .map { true }
}
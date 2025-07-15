package com.kominioai.domain.survey.application.port.out

import com.kominioai.domain.survey.domain.model.ParticipationId
import com.kominioai.domain.survey.domain.model.QuizParticipation
import reactor.core.publisher.Mono

interface CacheQuizParticipationPort {
    fun cacheParticipation(participation: QuizParticipation): Mono<Boolean>
    fun getCachedParticipation(participationId: ParticipationId): Mono<QuizParticipation?>
    fun invalidateParticipationCache(participationId: ParticipationId): Mono<Boolean>
    fun cacheParticipationList(participations: List<QuizParticipation>): Mono<Boolean>
}
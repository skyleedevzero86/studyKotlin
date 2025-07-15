package com.kominioai.domain.survey.adapter.out.persistence

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface QuizAnswerR2dbcRepository : ReactiveCrudRepository<QuizAnswerEntity, String> {
    fun findByParticipationId(participationId: String): reactor.core.publisher.Flux<QuizAnswerEntity>
    fun findByParticipationIdIn(participationIds: List<String>): reactor.core.publisher.Flux<QuizAnswerEntity>
    fun findByQuestionId(questionId: String): reactor.core.publisher.Flux<QuizAnswerEntity>
    fun deleteByParticipationId(participationId: String): Mono<Void>
    fun countByParticipationId(participationId: String): Mono<Long>
}
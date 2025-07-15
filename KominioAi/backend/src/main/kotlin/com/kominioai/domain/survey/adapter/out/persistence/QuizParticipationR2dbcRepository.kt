package com.kominioai.domain.survey.adapter.out.persistence

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

interface QuizParticipationR2dbcRepository : ReactiveCrudRepository<QuizParticipationEntity, String> {
    fun findBySurveyId(surveyId: String): reactor.core.publisher.Flux<QuizParticipationEntity>
    fun findBySurveyIdAndParticipantPhone(surveyId: String, phone: String): Mono<QuizParticipationEntity?>
    fun findByParticipantPhone(phone: String): reactor.core.publisher.Flux<QuizParticipationEntity>
    fun findByStatus(status: String): reactor.core.publisher.Flux<QuizParticipationEntity>
    fun findByStartedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): reactor.core.publisher.Flux<QuizParticipationEntity>
    fun countBySurveyId(surveyId: String): Mono<Long>
}
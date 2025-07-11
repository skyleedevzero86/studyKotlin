package com.kominioai.domain.survey.infrastructure.persistence.jpa.adapter

import com.kominioai.domain.survey.application.port.output.SurveyResponseRepository
import com.kominioai.domain.survey.domain.model.domain.SurveyResponse
import com.kominioai.domain.survey.domain.valueobject.ResponseId
import com.kominioai.domain.survey.domain.valueobject.SurveyId
import com.kominioai.domain.survey.infrastructure.persistence.jpa.entity.SurveyResponse as SurveyResponseEntity
import com.kominioai.domain.survey.infrastructure.persistence.jpa.repository.SurveyResponseJpaRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class JpaSurveyResponseRepositoryAdapter(
    private val jpaRepository: SurveyResponseJpaRepository
) : SurveyResponseRepository {

    override fun save(response: SurveyResponse): Mono<SurveyResponse> {
        val entity = SurveyResponseEntity.from(response)
        val savedEntity = jpaRepository.save(entity)
        return Mono.just(savedEntity.toDomain())
    }

    override fun findById(id: ResponseId): Mono<SurveyResponse> {
        val entity = jpaRepository.findById(id.value)
        return if (entity.isPresent) {
            Mono.just(entity.get().toDomain())
        } else {
            Mono.empty()
        }
    }

    override fun findBySurveyId(surveyId: SurveyId): Flux<SurveyResponse> {
        val entities = jpaRepository.findBySurveyId(surveyId.value)
        return Flux.fromIterable(entities.map { it.toDomain() })
    }

    override fun countBySurveyId(surveyId: SurveyId): Mono<Long> {
        val count = jpaRepository.countBySurveyId(surveyId.value)
        return Mono.just(count)
    }

    override fun findAll(): Flux<SurveyResponse> {
        val entities = jpaRepository.findAll()
        return Flux.fromIterable(entities.map { it.toDomain() })
    }

    override fun delete(id: ResponseId): Mono<Void> {
        jpaRepository.deleteById(id.value)
        return Mono.empty()
    }
}